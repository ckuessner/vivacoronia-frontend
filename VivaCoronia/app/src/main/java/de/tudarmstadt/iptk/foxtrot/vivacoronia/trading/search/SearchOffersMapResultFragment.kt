package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

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
    private var markers = mutableMapOf<LatLng, Marker>()
    private var selectedMarker: Marker? = null

    private val callback = OnMapReadyCallback { googleMap ->
        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { initialOffers ->
            googleMap.clear()
            markers = mutableMapOf()
            val offersByLocation = initialOffers.groupBy { it.location } // TODO group markers if they are closer than 10 meters?
            for ((location, offers) in offersByLocation) {
                if (location.latitude == 0.0 && location.longitude == 0.0)
                    continue

                val markerOptions = MarkerOptions().position(location)
                val marker = googleMap.addMarker(markerOptions)
                marker.tag = offers
                markers[location] = marker
            }
        })
        googleMap.setOnMapClickListener { selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker()) }
        mGoogleMap = googleMap
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
        val marker = markers[latLng]
        if (mGoogleMap == null || marker == null)
            return false
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
        selectedMarker = marker
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        return true
    }
}