package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.LocationUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchOffersFragment : Fragment(), SearchView.OnQueryTextListener, FilterOffersFragment.OnApplyQueryListener {
    private val _tag = "SearchOffersFragment"

    lateinit var viewModel: SearchOffersViewModel
    private lateinit var binding: FragmentSearchOffersBinding
    private lateinit var listResultFragment: SearchOffersListResultFragment
    private lateinit var mapResultFragment: SearchOffersMapResultFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        // needed on return to this fragment with onApplyQuery, because this method sometimes gets called after onApplyQuery
        val isLoading = this::binding.isInitialized && binding.progressHorizontal.visibility == View.VISIBLE
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers, container, false)
        if (isLoading)
            binding.progressHorizontal.visibility = View.VISIBLE

        viewModel = ViewModelProvider(requireActivity()).get(SearchOffersViewModel::class.java)
        if (viewModel.searchQuery.value == null) {
            viewModel.searchQuery.value = ProductSearchQuery()
            viewModel.searchQuery.value!!.location = LocationUtility.getLastKnownLocation(requireActivity())
        }

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
        viewModel.searchQuery.value!!.productName = query
        onApplyQuery(viewModel.searchQuery.value!!)
        binding.searchView.clearFocus()
        return true
    }

    override fun onApplyQuery(searchQuery: ProductSearchQuery) {
        binding.progressHorizontal.visibility = View.VISIBLE
        GlobalScope.launch {
            try {
                val offers = TradingApiClient.getOffers(requireContext(), searchQuery)
                requireActivity().runOnUiThread {
                    viewModel.searchResults.value = offers
                }
            } catch (e: VolleyError) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
                }
                Log.e(_tag, "Error while trying to fetch offers", e)
            }
            requireActivity().runOnUiThread {
                binding.progressHorizontal.visibility = View.INVISIBLE
            }
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // TODO do autocompletion
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val filterFragment = FilterOffersFragment.newInstance(this)
        parentFragmentManager.beginTransaction()
            .replace(this.id, filterFragment)
            .addToBackStack(null) // TODO funktioniert nicht
            .commit()
        return true
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