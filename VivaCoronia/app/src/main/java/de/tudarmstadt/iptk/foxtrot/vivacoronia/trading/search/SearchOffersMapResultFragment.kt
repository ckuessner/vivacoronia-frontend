package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.content.Context
import android.graphics.Color
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
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.view.DefaultClusterRenderer
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
    private lateinit var mClusterManager: ClusterManager<OfferClusterItem>
    private lateinit var mRenderer: CustomClusterRenderer
    private var currentViewedCluster: List<OfferClusterItem>? = null
    private var currentRadiusCircle: Circle? = null

    private val callback = OnMapReadyCallback { googleMap ->
        userLocation = LocationUtility.getLastKnownLocation(requireActivity()) ?: LatLng(0.0, 0.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15F)) // TODO set Zoom level to previous zoom level or to selected location

        parent.viewModel.searchQuery.observe(viewLifecycleOwner, Observer { drawRadius(googleMap)})

        mClusterManager = ClusterManager(requireContext(), googleMap)
        mClusterManager.algorithm = NonHierarchicalDistanceBasedAlgorithm<OfferClusterItem>().apply {
            maxDistanceBetweenClusteredItems = 50
        }
        mRenderer = CustomClusterRenderer(requireContext(), googleMap, mClusterManager)
        mClusterManager.renderer = mRenderer
        googleMap.setOnCameraIdleListener(mClusterManager)
        googleMap.setOnMarkerClickListener(mClusterManager)
        mClusterManager.setOnClusterClickListener { cluster ->
            showPopup(binding.anchorMenu, cluster.items.toMutableList())
            true
        }
        parent.viewModel.searchResults.observe(viewLifecycleOwner, Observer<List<Offer>> { initialOffers ->
            mGoogleMap?.clear()         // TODO Bei Suche ausführen -> karte anzeigen -> filtern -> radius einstellen -> anwenden wird ein Marker verschluckt
            drawRadius(googleMap)
            mClusterManager.clearItems()
            markers = mutableMapOf()
            for (offer in initialOffers) {
                if (offer.location.latitude == 0.0 && offer.location.longitude == 0.0)
                    continue

                val offerItem = OfferClusterItem(offer.location, offer.product, offer.details)
                mClusterManager.addItem(offerItem)
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