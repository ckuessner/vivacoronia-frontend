package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import com.androidmapsextensions.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSupermarketInventoryMapResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search.CustomClusterOptionsProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.LocationUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SupermarketInventoryMapResultFragment(private val parent: SupermarketInventoryFragment) : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(parent: SupermarketInventoryFragment) = SupermarketInventoryMapResultFragment(parent)
    }

    private lateinit var binding: FragmentSupermarketInventoryMapResultBinding
    private var mGoogleMap: GoogleMap? = null
    private var markers = mutableMapOf<String, Marker>()
    private var userLocation: LatLng? = null
    private var userZoom: Float = 15F
    private var showDialog: Boolean = false
    private var currentViewedCluster: List<Marker>? = null
    private var recenterMap = true

    private val callback = OnMapReadyCallback { googleMap ->
        mGoogleMap = googleMap
        if (recenterMap) {
            userLocation = LocationUtility.getLastKnownLocation(requireActivity()) ?: LatLng(0.0, 0.0)
        }
        recenterMap = false

        val clusteringSettings = ClusteringSettings()
            .minMarkersCount(2)
            .clusterOptionsProvider(CustomClusterOptionsProvider(resources))
        googleMap.setClustering(clusteringSettings)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, userZoom))

        googleMap.setOnMapLongClickListener {
            GlobalScope.launch {
                val response: List<PlacesApiResult> =
                    TradingApiClient.getSupermarkets(requireContext(), it, 3000.0)

                requireActivity().runOnUiThread {
                    parent.searchViewModel.supermarkets.value = response
                }
            }
            parent.searchViewModel.selectedMarker.value = null
        }
        googleMap.setOnMapClickListener {
            parent.searchViewModel.selectedMarker.value = null
        }
        googleMap.setOnInfoWindowClickListener { marker ->
            onInfoWindowClick(marker)
        }
        googleMap.setOnMarkerClickListener { marker ->
            if(marker.isCluster){
                showPopup(binding.anchorMenu, marker.markers)
            }
            else{
                parent.searchViewModel.selectedMarker.value = null
                marker.showInfoWindow()
            }
            true
        }
        parent.searchViewModel.supermarkets.observe(
            viewLifecycleOwner,
            Observer {
                googleMap.clear()
                for(supermarket in it){
                    val marker = googleMap.addMarker(MarkerOptions().title(supermarket.supermarketName).position(supermarket.supermarketLocation).data(supermarket))
                    markers[supermarket.supermarketPlaceId] = marker
                }
            }
        )

        parent.searchViewModel.errorData.observe(viewLifecycleOwner, Observer {
            if(it != null && showDialog){
                AlertDialog.Builder(requireContext(), R.style.AlterDialogTheme)
                    //.setTitle("Create new inventory item")
                    .setCancelable(true)
                    .setMessage("Create new inventory item for the selected supermarket?")
                    .setPositiveButton("Create") { _, _ ->
                        Toast.makeText(requireContext(), "Create Item for ${it.supermarketName}", Toast.LENGTH_SHORT)
                            .show()
                        EditInventoryItemActivity.start(
                            requireContext(),
                            InventoryItem(
                                "",
                                "",
                                "",
                                0,
                                it.supermarketPlaceId,
                                it.supermarketName,
                                it.supermarketLocation
                            ), newItem = true, newSupermarket = true
                        )
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .create()
                    .show()
            }
            showDialog = false
        })

        parent.searchViewModel.selectedMarker.observe(viewLifecycleOwner, Observer {
            findMarkerBySupermarketId(it)
            if(it != null){
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers[it]?.position, 15f))
            }
        })

        parent.inventoryViewModel.supermarketInventory.observe(viewLifecycleOwner, Observer {
            parent.switchFragments(1)
        })
    }

    private fun findMarkerBySupermarketId(supermarketId: String?) {
        if(supermarketId != null){
            markers[supermarketId]?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        }
        if(supermarketId == null){
            for(marker in markers.values){
                marker.setIcon(BitmapDescriptorFactory.defaultMarker())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_supermarket_inventory_map_result, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getExtendedMapAsync(callback)
    }

    private fun showPopup(view: View, supermarketCluster: List<Marker>) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenuStyle)
        val popup = PopupMenu(wrapper, view)
        currentViewedCluster = supermarketCluster

        popup.inflate(R.menu.search_item_cluster_info_window)
        for(i in supermarketCluster.indices) {
            popup.menu.add(1, i, 1, supermarketCluster[i].title)
        }
        popup.setOnMenuItemClickListener { item ->
            if (currentViewedCluster != null) {
                onInfoWindowClick(currentViewedCluster!![item.itemId])
            }
            true
        }
        popup.show()
    }

    private fun onInfoWindowClick(marker: Marker) {
        val supermarket = marker.getData<PlacesApiResult>()
        GlobalScope.launch {
            val response: Supermarket =
                TradingApiClient.getSupermarketInventoryForID(requireContext(), supermarket, ::setErrorCodeViewModel)

            requireActivity().runOnUiThread {
                parent.inventoryViewModel.supermarketInventory.value = response
            }
        }
    }

    private fun setErrorCodeViewModel(error: VolleyError, supermarket: PlacesApiResult){
        if(error.networkResponse.statusCode == 404){
            showDialog = true
            parent.searchViewModel.errorData.value = supermarket
        }
    }
}