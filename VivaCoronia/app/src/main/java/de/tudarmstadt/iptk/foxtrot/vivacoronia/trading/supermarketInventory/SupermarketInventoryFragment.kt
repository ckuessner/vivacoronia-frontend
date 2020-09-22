package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.SupermarketInventoryFragmentBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val ARG_ID = "id"

class SupermarketInventoryFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(
            supermarketId: String
        ) = SupermarketInventoryFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, supermarketId)
            }
        }

        private const val TAG = "SupermarketInventoryFragment"
    }

    lateinit var inventoryViewModel: SupermarketInventoryViewModel
    lateinit var searchViewModel: SupermarketSearchViewModel
    lateinit var binding: SupermarketInventoryFragmentBinding
    private lateinit var listResultFragment: SupermarketInventoryListResultFragment
    private lateinit var mapResultFragment: SupermarketInventoryMapResultFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentFragmentManager.beginTransaction().add(this, TAG)
        setHasOptionsMenu(true)
        val isLoading = this::binding.isInitialized && binding.progressHorizontal.visibility == View.VISIBLE
        binding = DataBindingUtil.inflate(inflater, R.layout.supermarket_inventory_fragment, container, false)
        if(isLoading)
            binding.progressHorizontal.visibility = View.VISIBLE
        inventoryViewModel = ViewModelProvider(requireActivity()).get(SupermarketInventoryViewModel::class.java)
        searchViewModel = ViewModelProvider(requireActivity()).get(SupermarketSearchViewModel::class.java)

        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.supermarketInventoryPager.adapter = pagerAdapter
        binding.supermarketInventoryPager.isUserInputEnabled = false
        binding.supermarketInventoryPager.offscreenPageLimit = 1
        binding.mapDisplay.setOnClickListener {
            binding.supermarketInventoryPager.setCurrentItem(0, true)
        }

        binding.supermarketInventoryDisplay.setOnClickListener {
            binding.supermarketInventoryPager.setCurrentItem(1, true)
        }

        arguments?.let {
            val supermarketId = it.getString(ARG_ID)
            if(supermarketId != null) {
                GlobalScope.launch {
                    val response: Supermarket =
                        TradingApiClient.getSupermarketInventoryForID(
                            requireContext(),
                            PlacesApiResult(supermarketId, "", LatLng(0.0, 0.0)),
                            ::onRequestFailed
                        )
                    requireActivity().runOnUiThread {
                        inventoryViewModel.supermarketInventory.value = response
                        binding.supermarketInventoryPager.setCurrentItem(1, false)
                        (activity?.findViewById(R.id.bottom_nav_view) as BottomNavigationView).selectedItemId =
                            R.id.supermarkets
                    }
                }
            }
        }
        return binding.root
    }

    private fun onRequestFailed(error: VolleyError, supermarket: PlacesApiResult){
        error.printStackTrace()
    }

    private inner class ScreenSlidePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val numPages = 2
        override fun getItemCount(): Int = numPages
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    mapResultFragment = SupermarketInventoryMapResultFragment.newInstance(this@SupermarketInventoryFragment)
                    mapResultFragment
                }
                1 -> {

                    listResultFragment = SupermarketInventoryListResultFragment.newInstance(this@SupermarketInventoryFragment)
                    listResultFragment
                }
                else -> throw IndexOutOfBoundsException("Invalid index ${position}, needs to be between 0 and ${numPages - 1}")
            }
        }
    }

    fun switchFragments(frag: Int){
        if(frag == 0){
            binding.supermarketInventoryDisplay.isChecked = false
            binding.mapDisplay.isChecked = true
            binding.supermarketInventoryPager.setCurrentItem(frag, true)
        }
        else if(frag == 1) {
            binding.supermarketInventoryDisplay.isChecked = true
            binding.mapDisplay.isChecked = false
            binding.supermarketInventoryPager.setCurrentItem(frag, true)
        }
    }
}