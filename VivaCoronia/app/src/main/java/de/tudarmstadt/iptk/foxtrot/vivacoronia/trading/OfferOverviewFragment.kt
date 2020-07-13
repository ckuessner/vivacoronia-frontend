package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        binding.add.setOnClickListener { AddOfferActivity.start(requireContext(), null) }

        return binding.root
    }

    private fun deleteOfferCallback(id: String) {
        val deleted = TradingApiClient.deleteOffer(id)
        if (deleted)
            viewModel.remove(id)
        // TODO better fetch again and reset whole list. Does not change efficiency because only differences matter
    }

    private fun editOfferCallback(offer: Offer){
        AddOfferActivity.start(requireContext(), offer)
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