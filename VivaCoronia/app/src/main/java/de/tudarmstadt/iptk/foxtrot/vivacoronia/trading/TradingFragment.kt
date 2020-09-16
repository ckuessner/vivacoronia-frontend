package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentTradingBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentTradingNavBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs.NeedOverviewFragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferOverviewFragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search.SearchOffersFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TradingFragment : Fragment() {
    companion object {
        private const val TAG = "Trading Fragment"
    }

    private lateinit var binding: FragmentTradingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trading, container, false)
        GlobalScope.launch { fetchCategories() }
        binding.retryConnection.setOnClickListener { onConnectionRetry() }


        return binding.root
    }

    private fun fetchCategories() {
        try {
            val categories = TradingApiClient.getAllCategories(requireContext()).toMutableList()
            activity?.runOnUiThread {
                BaseProduct.categories.value = categories
            } ?: showRetry()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories!", e)
            showRetry()
        }
    }

    private fun showRetry() {
        activity?.runOnUiThread {
            binding.loadingProgressBar.visibility = View.INVISIBLE
            binding.loadingText.visibility = View.INVISIBLE
            binding.connectionFailedIcon.visibility = View.VISIBLE
            binding.connectionFailedText.visibility = View.VISIBLE
            binding.retryConnection.isEnabled = true
        }
    }

    private fun onConnectionRetry() {
        activity?.runOnUiThread {
            binding.loadingProgressBar.visibility = View.VISIBLE
            binding.loadingText.visibility = View.VISIBLE
            binding.connectionFailedIcon.visibility = View.INVISIBLE
            binding.connectionFailedText.visibility = View.INVISIBLE
            binding.retryConnection.isEnabled = false
        }
        GlobalScope.launch { fetchCategories() }
    }
}

class TradingFragmentNav : Fragment(), Observer<MutableList<String>> {
    private lateinit var binding : FragmentTradingNavBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val notificationQuery = arguments?.getParcelable<ProductSearchQuery>("product_query")
        Log.i("TradingNavFragment", "query: $notificationQuery")

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trading_nav, container, false)
        binding.bottomNavView.selectedItemId = R.id.search_offers
        if (BaseProduct.categories.value == null){
            binding.bottomNavView.isEnabled = false
            BaseProduct.categories.observe(viewLifecycleOwner, this)
        } else {
            loadFragment(R.id.search_offers, notificationQuery)
        }
        binding.bottomNavView.setOnNavigationItemSelectedListener { loadFragment(it.itemId, null) }

        return binding.root
    }

    private fun loadFragment(item: Int, query: ProductSearchQuery?): Boolean {
        if (!binding.bottomNavView.isEnabled)
            return false

        val fragment = when (item) {
            R.id.search_offers -> SearchOffersFragment::class.java
            R.id.my_offers -> OfferOverviewFragment::class.java
            R.id.my_needs -> NeedOverviewFragment::class.java
            else -> null
        }

        val queryBundle = Bundle()
        if (fragment == SearchOffersFragment::class.java && query != null) {
            queryBundle.putParcelable("product_query", query)
        }


        if (fragment != null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, queryBundle)
                .commit()
            return true
        }
        return false
    }

    override fun onChanged(newList: MutableList<String>?) {
        binding.bottomNavView.isEnabled = true
        binding.bottomNavView.selectedItemId = R.id.search_offers
        binding.navHostFragment.findNavController().navigate(R.id.search_offers)
        BaseProduct.categories.removeObserver(this)
    }
}