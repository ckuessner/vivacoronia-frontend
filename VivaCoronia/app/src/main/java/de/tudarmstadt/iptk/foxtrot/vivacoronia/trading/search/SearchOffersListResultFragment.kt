package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersListResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferListViewModel
import kotlin.math.*

class SearchOffersListResultFragment(private val parent: SearchOffersFragment) : Fragment() {
    private lateinit var binding: FragmentSearchOffersListResultBinding

    private lateinit var offerDistances: ArrayList<Double>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers_list_result, container, false)

        binding.offerListViewModel = ViewModelProvider(requireActivity()).get(OfferListViewModel::class.java)
        val adapter = SearchResultAdapter(SearchResultItemListener { parent.viewModel.onOfferDetailClick(it) })
        binding.resultList.adapter = adapter

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> {
            binding.offerListViewModel!!.setOffers(it ?: listOf())
            binding.offerListViewModel!!.setDistances(getLastKnownLocation())
        })
        binding.offerListViewModel!!.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(parent: SearchOffersFragment) = SearchOffersListResultFragment(parent)
    }

    private fun getLastKnownLocation(): LatLng?{
        var lastKnownLocation: LatLng? = null
        if (PermissionHandler.checkLocationPermissions(requireActivity())) {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            @SuppressLint("MissingPermission") // Check is in PermissionHandler
            var currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            @SuppressLint("MissingPermission")
            if(currentLocation == null){
                currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            if (currentLocation != null)
                lastKnownLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
        }
        return lastKnownLocation
    }

    fun scrollToOffer(id: String) {
        val offers = parent.viewModel.searchResults.value
        var offerByID: Pair<Int, Offer?> = Pair(0, null)
        if(offers != null){
            offerByID = findOfferByID(id, offers)
        }
        if(offerByID.second != null){
            binding.resultList.scrollToPosition(offerByID.first)
        }
    }

    private fun findOfferByID(id: String, offers: List<Offer>): Pair<Int, Offer?> {
        var offerAndPos: Pair<Int, Offer?> = Pair(0, null)
        for(offerPos in offers.indices){
            if(offers[offerPos].id == id){
                offerAndPos = Pair(offerPos, offers[offerPos])
            }
        }
        return offerAndPos
    }
}