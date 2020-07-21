package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentOfferOverviewBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

private const val TAG = "OfferOverviewFragment"

class OffersFragment : Fragment() {
    private lateinit var binding: FragmentOfferOverviewBinding
    private lateinit var viewModel: OfferListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offer_overview, container, false)
        viewModel = ViewModelProvider(this).get(OfferListViewModel::class.java)

        val adapter = OffersAdapter(::deleteOfferCallback, ::editOfferCallback)
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0)
                    binding.offersList.layoutManager!!.smoothScrollToPosition(binding.offersList, null, 0)
            }
        })

        binding.offersList.adapter = adapter
        viewModel.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })

        Offer.categories.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty())
                binding.add.isEnabled = true
        })
        binding.add.isEnabled = !Offer.categories.value.isNullOrEmpty() // Only allow adding offers if we fetched + received categories

        binding.offersListSwipeRefresh.setOnRefreshListener {
            GlobalScope.launch { fetchMyOffers() }
            GlobalScope.launch { fetchCategories() }
        }

        GlobalScope.launch { fetchCategories() }

        binding.add.setOnClickListener { SubmitOfferActivity.start(requireContext(), null) }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.offersListSwipeRefresh.post {
            binding.offersListSwipeRefresh.isRefreshing = true
            GlobalScope.launch { fetchMyOffers() }
        }
    }

    private fun deleteOfferCallback(id: String) {
        val dialog = activity?.let {
            AlertDialog.Builder(it)
                .setCancelable(true)
                .setPositiveButton(R.string.yes) { _, _ ->
                    binding.offersListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, false)
                    }
                }
                .setNegativeButton(R.string.yes_offer_sold) { _, _ ->
                    binding.offersListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, true)
                    }
                }
                .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setTitle(R.string.delete_offer_title)
                .setMessage(R.string.confirm_delete_message)
                .show()
        }?: return

        styleDialogButtons(listOf(
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        )) // TODO löschen wenn styling auch ohne so funktioniert wie bei Timo
    }

    private fun performDelete(id: String, sold: Boolean) {
        try {
            val deleted = TradingApiClient.deleteOffer(id, sold, requireContext())
            if (deleted)
                fetchMyOffers()
            /*requireActivity().runOnUiThread {
                                    viewModel.remove(id)
                                }*/
            // TODO what's the better option? refetch needs more data/computation, removing theoretically result in an inconsistent state
        } catch (e: Exception) {
            Log.e(TAG, "Unable to delete offer with id \"$id\"", e)
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Unable to delete offer", Toast.LENGTH_SHORT).show()
            }
        }
        binding.offersListSwipeRefresh.isRefreshing = false
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

    private fun editOfferCallback(offer: Offer){
        SubmitOfferActivity.start(requireContext(), offer)
    }

    private fun fetchCategories() {
        try {
            val categories = TradingApiClient.getAllCategories(requireContext()).toMutableList()
            requireActivity().runOnUiThread {
                Offer.categories.value = categories
            }
        } catch (e: Exception) {
            // Don't care if we already have categories
            if (!Offer.categories.value.isNullOrEmpty())
                return
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

    private fun fetchMyOffers() {
        try {
            val offers = TradingApiClient.getMyOffers(requireContext())
            requireActivity().runOnUiThread { viewModel.setOffers(offers) }
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }
            else {
                Log.e(TAG, "Error while fetching or parsing myOffers", exception)
            }
        }
        binding.offersListSwipeRefresh.isRefreshing = false
    }
}