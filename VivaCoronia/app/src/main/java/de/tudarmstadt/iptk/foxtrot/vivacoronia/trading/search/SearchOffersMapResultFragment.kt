package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersMapResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

class SearchOffersMapResultFragment(private val parent: SearchOffersFragment) : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(parent: SearchOffersFragment) = SearchOffersMapResultFragment(parent)
    }

    private lateinit var binding: FragmentSearchOffersMapResultBinding
    private var mGoogleMap: GoogleMap? = null
    private var markers = mutableMapOf<LatLng, Pair<String, Marker>>()
    private var selectedMarker: Marker? = null
    private var userLocation: LatLng? = null

    private val callback = OnMapReadyCallback { googleMap ->

        userLocation = LatLng(0.0, 0.0)
        if (PermissionHandler.checkLocationPermissions(requireActivity())) {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            @SuppressLint("MissingPermission") // Check is in PermissionHandler
            var currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            @SuppressLint("MissingPermission")
            if(currentLocation == null){
                currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            if (currentLocation != null)
                userLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15F))

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { initialOffers ->
            googleMap.clear()
            markers = mutableMapOf()
            val offersByLocation = initialOffers.groupBy { it.location } // TODO group markers if they are closer than 10 meters?
            for ((location, offers) in offersByLocation) {
                if (location.latitude == 0.0 && location.longitude == 0.0)
                    continue

                val markerOptions = MarkerOptions().position(location).title(offers[0].product).snippet(offers[0].details)
                val marker = googleMap.addMarker(markerOptions)
                marker.tag = offers
                markers[location] = Pair(offers[0].id, marker)
            }
        })
        googleMap.setOnMapClickListener {
            selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
            selectedMarker = null
        }
        googleMap.setOnInfoWindowClickListener { marker ->
            onInfoWindowClick(marker)
        }
        mGoogleMap = googleMap
    }

    private fun onInfoWindowClick(marker: Marker?) {
        if(marker != null) {
            onMarkerClick(marker)
        }
    }

    private fun onMarkerClick(marker: Marker) {
        val offerID = markers[marker.position]!!.first
        parent.viewModel.onOfferMarkerClick(offerID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers_map_result, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    fun highlightOfferOnMap(latLng: LatLng): Boolean {
        val markerPair = markers[latLng]
        if (mGoogleMap == null || markerPair == null)
            return false
        val marker = markerPair.second
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
        selectedMarker = marker
        selectedMarker!!.showInfoWindow()
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        return true
    }
}