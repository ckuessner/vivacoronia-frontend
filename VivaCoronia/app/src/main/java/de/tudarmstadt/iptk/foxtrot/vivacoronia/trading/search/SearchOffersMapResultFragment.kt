package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.graphics.Color
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.androidmapsextensions.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSearchOffersMapResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.LocationUtility


class SearchOffersMapResultFragment(private val parent: SearchOffersFragment) : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(parent: SearchOffersFragment) = SearchOffersMapResultFragment(parent)
    }

    private lateinit var binding: FragmentSearchOffersMapResultBinding
    private var mGoogleMap: GoogleMap? = null
    private var markers = mutableMapOf<LatLng, Pair<String, OfferClusterItem>>()
    private var selectedOfferItem: OfferClusterItem? = null
    private var userLocation: LatLng? = null
    private var userZoom: Float = 15F
    private var currentViewedCluster: List<Marker>? = null
    private var currentRadiusCircle: Circle? = null
    private var recenterMap = true

    private val callback = OnMapReadyCallback { googleMap ->
        if (recenterMap) {
            userLocation = LocationUtility.getLastKnownLocation(requireActivity()) ?: LatLng(0.0, 0.0)
        }
        recenterMap = false
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, userZoom))

        parent.viewModel.searchQuery.observe(viewLifecycleOwner, Observer {
            drawRadius(googleMap)
        })
        val clusteringSettings = ClusteringSettings()
            .minMarkersCount(2)
            .clusterOptionsProvider(CustomClusterOptionsProvider(resources))
        googleMap.setClustering(clusteringSettings)

        googleMap.setOnCameraMoveListener {
            userLocation = googleMap.cameraPosition.target
            userZoom = googleMap.cameraPosition.zoom
        }

        googleMap.setOnMapClickListener { deselectCurrentMarker() }
        googleMap.setOnMarkerClickListener { onMarkerClick(it) }
        googleMap.setOnInfoWindowClickListener { onInfoWindowClick(it) }
        mGoogleMap = googleMap

        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { populateMap(googleMap, it) })
    }

    private fun onMarkerClick(marker: Marker): Boolean {
        if (selectedMarker != null && !selectedMarker!!.isCluster) {
            selectedMarker!!.setIcon(BitmapDescriptorFactory.defaultMarker())
        }
        selectedMarker = marker
        if (marker.isCluster) {
            showPopup(binding.anchorMenu, marker.markers)
        } else {
            selectedMarker!!.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            marker.showInfoWindow()
            selectedMarker = marker
        }
        return true
    }

    private fun deselectCurrentMarker() {
        if (selectedMarker != null && !selectedMarker!!.isCluster) {
            selectedMarker!!.setIcon(BitmapDescriptorFactory.defaultMarker())
            selectedMarker!!.hideInfoWindow()
        }
        selectedMarker = null
    }

    private fun populateMap(googleMap: GoogleMap, initialOffers: List<Offer>) {
        currentViewedCluster = null
        selectedMarker = null
        googleMap.clear()
        markers = mutableMapOf()
        for (offer in initialOffers) {
            if (offer.location.latitude == 0.0 && offer.location.longitude == 0.0)
                continue
            val markerOptions = MarkerOptions()
                .position(offer.location)
                .title(offer.product)
                .snippet(offer.details)
            val marker = googleMap.addMarker(markerOptions)
            markers[offer.location] = Pair(offer.id, marker)
        }
        drawRadius(googleMap)
    }

    private fun drawRadius(googleMap: GoogleMap?) {
        if (googleMap == null)
            return

        currentRadiusCircle?.remove()
        val location = parent.viewModel.searchQuery.value?.location
        val radiusInKm = parent.viewModel.searchQuery.value?.radiusInKm
        if (location == null || location == LatLng(0.0, 0.0) || radiusInKm == null || radiusInKm <= 0)
            return
        val circleOptions = CircleOptions()
            .center(location)
            .radius(radiusInKm.toDouble() * 1000)
            .fillColor(Color.parseColor("#447aff85"))
            .strokeColor(Color.BLACK)
            .strokeWidth(3F)
        currentRadiusCircle = googleMap.addCircle(circleOptions)
    }

    private fun onInfoWindowClick(marker: Marker) {
        val offerID = markers[marker.position]!!.first
        parent.viewModel.onOfferMarkerClick(offerID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_offers_map_result, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getExtendedMapAsync(callback)
    }

    fun highlightOfferOnMap(latLng: LatLng): Boolean {
        deselectCurrentMarker()
        val markerPair = markers[latLng]
        if (mGoogleMap == null || markerPair == null)
            return false
        val marker = markerPair.second
        selectedMarker = marker
        if (!marker.isCluster) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            marker.showInfoWindow()
        }

        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        return true
    }

    private fun showPopup(view: View, offerItems: List<Marker>) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenuStyle)
        val popup = PopupMenu(wrapper, view)
        currentViewedCluster = offerItems

        popup.inflate(R.menu.search_item_cluster_info_window)
        for(i in offerItems.indices) {
            popup.menu.add(1, i, 1, offerItems[i].title)
        }
        popup.setOnMenuItemClickListener { item ->
            if (currentViewedCluster != null) {
                onInfoWindowClick(currentViewedCluster!![item.itemId])
            }
            true
        }
        popup.show()
    }
}

class OfferClusterItem(latLng: LatLng, title: String, snippet: String) : ClusterItem{
    private val mPosition: LatLng = latLng
    private val mTitle: String = title
    private val mSnippet: String = snippet

    override fun getSnippet(): String {
        return mSnippet
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun equals(other: Any?): Boolean {
        return if(other is OfferClusterItem){
            this.hashCode() == other.hashCode()
        } else{
            false
        }
    }

    override fun hashCode(): Int {
        var result = mPosition.hashCode()
        result = 31 * result + mTitle.hashCode()
        result = 31 * result + mSnippet.hashCode()
        return result
    }
}

class CustomClusterRenderer(
    val context: Context,
    val map: GoogleMap,
    clusterManager: ClusterManager<OfferClusterItem>
): DefaultClusterRenderer<OfferClusterItem>(context, map, clusterManager) {
    var selectedItem: OfferClusterItem? = null

    override fun onBeforeClusterItemRendered(item: OfferClusterItem, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        if(selectedItem != null && item == selectedItem){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        }
    }

    override fun shouldRenderAsCluster(cluster: Cluster<OfferClusterItem>): Boolean {
        return super.shouldRenderAsCluster(cluster) || cluster.size > 1
    }
}