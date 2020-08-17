package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
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
    private var markers = mutableMapOf<LatLng, Pair<String, OfferClusterItem>>()
    //private var selectedMarker: Marker? = null
    private var selectedOfferItem: OfferClusterItem? = null
    private var userLocation: LatLng? = null
    private lateinit var mClusterManager: ClusterManager<OfferClusterItem>
    private lateinit var mRenderer: CustomClusterRenderer
    private var currentViewedCluster: List<OfferClusterItem>? = null

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

        mClusterManager = ClusterManager(requireContext(), googleMap)
        mRenderer = CustomClusterRenderer(requireContext(), googleMap, mClusterManager)
        mClusterManager.renderer = mRenderer
        googleMap.setOnCameraIdleListener(mClusterManager)
        googleMap.setOnMarkerClickListener(mClusterManager)
        mClusterManager.setOnClusterClickListener { cluster ->
            showPopup(binding.anchorMenu, cluster.items.toMutableList())
            true
        }
        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { initialOffers ->
            //googleMap.clear()
            mClusterManager.clearItems()
            markers = mutableMapOf()
            //val offersByLocation = initialOffers.groupBy { it.location } // TODO group markers if they are closer than 10 meters?
            for (offer in initialOffers) {
                if (offer.location.latitude == 0.0 && offer.location.longitude == 0.0)
                    continue

                //val markerOptions = MarkerOptions().position(location).title(offers[0].product).snippet(offers[0].details)
                //val marker = googleMap.addMarker(markerOptions)
                val offerItem = OfferClusterItem(offer.location, offer.product, offer.details)
                mClusterManager.addItem(offerItem)
                //marker.tag = offers
                markers[offer.location] = Pair(offer.id, offerItem)
            }
            mClusterManager.cluster()
        })
        googleMap.setOnMapClickListener {
            mRenderer.getMarker(selectedOfferItem)?.setIcon(BitmapDescriptorFactory.defaultMarker())
            selectedOfferItem = null
            mRenderer.selectedItem = null
        }
        mClusterManager.setOnClusterItemInfoWindowClickListener { offerItem ->
            onInfoWindowClick(offerItem)
        }
        mGoogleMap = googleMap
    }

    private fun onInfoWindowClick(offerItem: OfferClusterItem) {
        onMarkerClick(offerItem)
    }

    private fun onMarkerClick(offerItem: OfferClusterItem) {
        val offerID = markers[offerItem.position]!!.first
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
        val offerItem = markerPair.second
        mRenderer.getMarker(selectedOfferItem)?.setIcon(BitmapDescriptorFactory.defaultMarker())
        mRenderer.getMarker(selectedOfferItem)?.hideInfoWindow()
        val marker = mRenderer.getMarker(offerItem)
        if(marker != null){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            //selectedMarker?.setIcon(BitmapDescriptorFactory.defaultMarker())
            marker.showInfoWindow()
        }
        selectedOfferItem = offerItem
        mRenderer.selectedItem = offerItem
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        return true
    }

    private fun showPopup(view: View, offerItems: List<OfferClusterItem>) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenuStyle)
        val popup = PopupMenu(wrapper, view)
        currentViewedCluster = offerItems
        popup.inflate(R.menu.search_item_cluster_info_window)
        for(i in offerItems.indices){
            popup.menu.add(1, i, 1, offerItems[i].title)
        }
        popup.setOnMenuItemClickListener { item ->
            if(currentViewedCluster != null) {
                onMarkerClick(currentViewedCluster!![item.itemId])
            }
            true
        }
        popup.show()
    }
}

class OfferClusterItem: ClusterItem{
    private val mPosition: LatLng
    private val mTitle: String
    private val mSnippet: String

    constructor(latLng: LatLng){
        mPosition = latLng
        mTitle = ""
        mSnippet = ""
    }

    constructor(latLng: LatLng, title: String, snippet: String){
        mPosition = latLng
        mTitle = title
        mSnippet = snippet
    }

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
}