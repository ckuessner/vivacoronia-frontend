package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

        binding.needsListSwipeRefresh.setOnRefreshListener { GlobalScope.launch { fetchMyNeeds() } }
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
        activity?.let {
            AlertDialog.Builder(it, R.style.AlertDialogTheme)
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
    }

    private fun performDelete(id: String, fulfilled: Boolean) {
        try {
            val deleted = TradingApiClient.deleteNeed(id, fulfilled, requireContext())
            if (deleted)
                fetchMyNeeds()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to delete need with id \"$id\"", e)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Unable to delete need", Toast.LENGTH_SHORT).show()
            }
        }
        activity?.runOnUiThread { binding.needsListSwipeRefresh.isRefreshing = false }
    }

    private fun fetchMyNeeds() {
        var textVisibility = TextView.VISIBLE
        try {
            activity?.let{
                val needs = TradingApiClient.getMyNeeds(it)
                if (needs.size > 0) textVisibility = TextView.INVISIBLE
                it.runOnUiThread { viewModel.setNeeds(needs) }
            }
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && activity?.hasWindowFocus() == true)
                activity?.let{ it.runOnUiThread {
                    Toast.makeText(it, R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }}
            else {
                Log.e(TAG, "Error while fetching or parsing myOffers", exception)
            }
        }
        activity?.runOnUiThread {
            binding.needsListSwipeRefresh.isRefreshing = false
            binding.noNeedsFound.visibility = textVisibility
        }
    }
}