package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val adapter = SearchResultAdapter(SearchResultItemListener { parent.viewModel.onOfferDetailClick(it) })
        binding.resultList.adapter = adapter

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { binding.offerListViewModel!!.setOffers(it ?: listOf()) })
        binding.offerListViewModel!!.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(parent: SearchOffersFragment) = SearchOffersListResultFragment(parent)
    }

    fun scrollToOffer(id: String) {
        // TODO
        Toast.makeText(requireActivity(), "Scrolling to $id", Toast.LENGTH_LONG).show()
    }
}