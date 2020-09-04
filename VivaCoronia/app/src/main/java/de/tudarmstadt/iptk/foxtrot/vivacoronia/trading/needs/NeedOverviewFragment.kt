package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.setMargins
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentNeedOverviewBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.SubmitProductActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

private const val TAG = "NeedOverviewFragment"

class NeedOverviewFragment : Fragment() {

    private lateinit var binding: FragmentNeedOverviewBinding
    private lateinit var viewModel: NeedListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_need_overview, container, false)
        viewModel = ViewModelProvider(this).get(NeedListViewModel::class.java)

        val adapter = NeedsAdapter(::deleteNeedCallback)
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0)
                    binding.needsList.layoutManager!!.smoothScrollToPosition(binding.needsList, null, 0)
            }
        })

        binding.needsList.adapter = adapter
        viewModel.needsList.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })

        BaseProduct.categories.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty())
                binding.add.isEnabled = true
        })
        binding.add.isEnabled = !BaseProduct.categories.value.isNullOrEmpty() // Only allow adding offers if we fetched + received categories

        binding.needsListSwipeRefresh.setOnRefreshListener {
            GlobalScope.launch { fetchMyNeeds() }
            GlobalScope.launch { fetchCategories() }
        }

        GlobalScope.launch { fetchCategories() }

        binding.add.setOnClickListener { SubmitProductActivity.start(requireContext(), null, false) }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.needsListSwipeRefresh.post {
            binding.needsListSwipeRefresh.isRefreshing = true
            GlobalScope.launch { fetchMyNeeds() }
        }
    }

    private fun deleteNeedCallback(id: String) {
        val dialog = activity?.let {
            AlertDialog.Builder(it)
                .setCancelable(true)
                .setPositiveButton(R.string.yes) { _, _ ->
                    binding.needsListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, false)
                    }
                }
                .setNegativeButton(R.string.yes_need_fulfilled) { _, _ ->
                    binding.needsListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, true)
                    }
                }
                .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setTitle(R.string.delete_need_title)
                .setMessage(R.string.confirm_delete_message_need)
                .show()
        }?: return

        styleDialogButtons(listOf(
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        )) // TODO löschen wenn styling auch ohne so funktioniert wie bei Timo
    }

    private fun performDelete(id: String, fulfilled: Boolean) {
        try {
            val deleted = TradingApiClient.deleteNeed(id, fulfilled, requireContext())
            if (deleted)
                fetchMyNeeds()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to delete need with id \"$id\"", e)
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Unable to delete need", Toast.LENGTH_SHORT).show()
            }
        }
        requireActivity().runOnUiThread { binding.needsListSwipeRefresh.isRefreshing = false }
    }

    private fun styleDialogButtons(buttons: List<Button>) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5)

        for (button in buttons) {
            button.setTextColor(Color.BLACK)
            button.setBackgroundColor(Color.LTGRAY)
            button.layoutParams = params
        }
    }

    private fun fetchCategories() {
        try {
            val categories = TradingApiClient.getAllCategories(requireContext()).toMutableList()
            requireActivity().runOnUiThread {
                BaseProduct.categories.value = categories
            }
        } catch (e: Exception) {
            // Don't care if we already have categories
            if (!BaseProduct.categories.value.isNullOrEmpty())
                return

            if (requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    activity?.let {
                        val dialog = AlertDialog.Builder(it)
                            .setMessage("Please make sure you have a working Internet connection and try again.")
                            .setTitle("No Internet")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()}
                            .show()
                        styleDialogButtons(listOf(dialog.getButton(AlertDialog.BUTTON_POSITIVE))) // TODO löschen wenn styling auch ohne so funktioniert wie bei Timo
                    }
                }
        }
    }

    private fun fetchMyNeeds() {
        try {
            val needs = TradingApiClient.getMyNeeds(requireContext())
            requireActivity().runOnUiThread { viewModel.setNeeds(needs) }
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }
            else {
                Log.e(TAG, "Error while fetching or parsing myOffers", exception)
            }
        }
        requireActivity().runOnUiThread { binding.needsListSwipeRefresh.isRefreshing = false }
    }
}