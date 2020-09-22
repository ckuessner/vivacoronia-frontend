package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersListResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.TradingFragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.TradingFragmentNav
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferListViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory.SupermarketInventoryFragment


class SearchOffersListResultFragment(private val parent: SearchOffersFragment) : Fragment() {
    private lateinit var binding: FragmentSearchOffersListResultBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_search_offers_list_result,
            container,
            false
        )

        binding.offerListViewModel = ViewModelProvider(requireActivity()).get(OfferListViewModel::class.java)
        val adapter = SearchResultAdapter(
            SearchResultItemListener { parent.viewModel.onOfferDetailClick(it) },
            SearchResultCallListener { parent.viewModel.onCallButtonClick(it, requireActivity()) },
            SearchResultSwitchToSupermarketListener { switchToSupermarketList(it) })
        binding.resultList.adapter = adapter

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> {
            binding.offerListViewModel!!.setOffers(it ?: listOf())
        })
        binding.offerListViewModel!!.offersList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(
                    it
                )
            }
        })

        return binding.root
    }

    private fun switchToSupermarketList(supermarketId: String) {
        val frag = SupermarketInventoryFragment.newInstance(supermarketId)
        parent.parentFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, frag).commit()
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