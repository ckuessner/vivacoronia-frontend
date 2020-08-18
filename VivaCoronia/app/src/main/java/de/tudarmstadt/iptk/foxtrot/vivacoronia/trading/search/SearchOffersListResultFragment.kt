package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersListResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferListViewModel

class SearchOffersListResultFragment(private val parent: SearchOffersFragment) : Fragment() {
    private lateinit var binding: FragmentSearchOffersListResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers_list_result, container, false)

        binding.offerListViewModel = ViewModelProvider(requireActivity()).get(OfferListViewModel::class.java)
        val adapter = SearchResultAdapter(SearchResultItemListener { parent.viewModel.onOfferDetailClick(it) }, SearchResultCallListener { parent.viewModel.onCallButtonClick(it, requireActivity()) })
        binding.resultList.adapter = adapter

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> {
            binding.offerListViewModel!!.setOffers(it ?: listOf())
        })
        binding.offerListViewModel!!.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(parent: SearchOffersFragment) = SearchOffersListResultFragment(parent)
    }

    fun scrollToOffer(id: String) {
        val offers = parent.viewModel.searchResults.value
        var offerByID: Pair<Int, Offer?> = Pair(0, null)
        if (offers != null) {
            offerByID = findOfferByID(id, offers)
        }
        if (offerByID.second != null) {
            binding.resultList.scrollToPosition(offerByID.first)
        }
    }

    private fun findOfferByID(id: String, offers: List<Offer>): Pair<Int, Offer?> {
        var offerAndPos: Pair<Int, Offer?> = Pair(0, null)
        for (offerPos in offers.indices) {
            if (offers[offerPos].id == id) {
                offerAndPos = Pair(offerPos, offers[offerPos])
            }
        }
        return offerAndPos
    }
}