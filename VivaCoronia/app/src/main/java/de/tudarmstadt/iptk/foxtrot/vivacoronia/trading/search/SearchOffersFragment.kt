package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchOffersFragment : Fragment(), SearchView.OnQueryTextListener {
    private val _tag = "SearchOffersFragment"

    var viewModel: SearchOffersViewModel = SearchOffersViewModel()
    private lateinit var binding: FragmentSearchOffersBinding
    private lateinit var listResultFragment: SearchOffersListResultFragment
    private lateinit var mapResultFragment: SearchOffersMapResultFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers, container, false)
        binding.searchView.setOnQueryTextListener(this)

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.searchResultsPager.adapter = pagerAdapter
        binding.searchResultsPager.isUserInputEnabled = false
        binding.searchResultsPager.offscreenPageLimit = 1 // Needed to initialize the map at startup

        binding.listDisplay.setOnClickListener {
            binding.searchResultsPager.setCurrentItem(0, true)
        }

        binding.mapDisplay.setOnClickListener {
            binding.searchResultsPager.setCurrentItem(1, true)
        }

        viewModel.highlightOfferOnMap.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.mapDisplay.performClick()
                mapResultFragment.highlightOfferOnMap(it.location)
                viewModel.onOfferDetailClickNavigated()
            }
        })

        viewModel.showOfferInList.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.listDisplay.performClick()
                listResultFragment.scrollToOffer(it.id)
                viewModel.onOfferMarkerClickNavigated()
            }
        })

        return binding.root
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        binding.progressHorizontal.visibility = View.VISIBLE
        val productSearchQuery = ProductSearchQuery()
        productSearchQuery.productName = query
        GlobalScope.launch {
            try {
                val offers = TradingApiClient.getOffers(requireContext(), productSearchQuery)
                requireActivity().runOnUiThread {
                    viewModel.searchResults.value = offers
                }
            } catch (e: VolleyError) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
                }
                Log.e(_tag, "Error while trying to fetch offers", e)
            } finally {
                binding.progressHorizontal.visibility = View.INVISIBLE
            }
        }
        binding.searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // TODO do autocompletion
        return false
    }

    private inner class ScreenSlidePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val numPages = 2
        override fun getItemCount(): Int = numPages
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    listResultFragment = SearchOffersListResultFragment.newInstance(this@SearchOffersFragment)
                    listResultFragment
                }
                1 -> {
                    mapResultFragment = SearchOffersMapResultFragment.newInstance(this@SearchOffersFragment)
                    mapResultFragment
                }
                else -> throw IndexOutOfBoundsException("Invalid index ${position}, needs to be between 0 and ${numPages - 1}")
            }
        }
    }
}