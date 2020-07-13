package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.setMargins
import androidx.databinding.DataBindingUtil
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

        binding.offersListSwipeRefresh.setOnRefreshListener {
            GlobalScope.launch {
                fetchMyOffers()
                binding.offersListSwipeRefresh.isRefreshing = false
            }
        }

        GlobalScope.launch { fetchMyOffers() }

        binding.add.setOnClickListener { SubmitOfferActivity.start(requireContext(), null) }

        return binding.root
    }

    private fun deleteOfferCallback(id: String) {
        val dialogBuilder = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setCancelable(true)
                setPositiveButton(R.string.yes) {_, _ ->
                    val deleted = TradingApiClient.deleteOffer(id)
                    if (deleted)
                        viewModel.remove(id)
                    // TODO better fetch again and reset whole list. Does not change efficiency because only differences matter
                }
                setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                setTitle(R.string.delete_offer_title)
                setMessage(R.string.confirm_delete_message)
            }
        }
            ?: return
        val dialog = dialogBuilder.create()
        dialog.show()
        styleDeleteDialog(dialog) // TODO löschen wenn styling auch ohne so funktioniert wie bei Timo
    }

    private fun styleDeleteDialog(dialog: AlertDialog) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5)

        val buttons = listOf(
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE),
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        )
        for (button in buttons) {
            button.setTextColor(Color.BLACK)
            button.setBackgroundColor(Color.LTGRAY)
            button.layoutParams = params
        }
    }

    private fun editOfferCallback(offer: Offer){
        SubmitOfferActivity.start(requireContext(), offer)
    }

    private fun fetchMyOffers() {
        try {
            val offers = TradingApiClient.getMyOffers()
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
    }
}