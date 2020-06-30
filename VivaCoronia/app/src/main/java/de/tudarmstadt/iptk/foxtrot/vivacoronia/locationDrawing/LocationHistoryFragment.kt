package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.jakewharton.threetenabp.AndroidThreeTen
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentLocationHistoryBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class LocationHistoryFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private lateinit var viewModel: LocationHistoryViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Double = 0.1

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        var selectionStart: Long = now.timeInMillis
        var selectionEnd: Long = now.timeInMillis

        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
        builder.setTitleText("Select a tracking period")
        val picker = builder.build()

        getGeoJSONFromServer(googleMap, false, selectionStart, selectionEnd)

        binding.datePickerBtn.setOnClickListener {
            picker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
        picker.addOnPositiveButtonClickListener {
            selectionStart = picker.selection?.first!!
            selectionEnd = picker.selection?.second!!
            googleMap.clear()
            getGeoJSONFromServer(googleMap, true, selectionStart, selectionEnd)
        }

        binding.reset.setOnClickListener {
            googleMap.clear()
            getGeoJSONFromServer(googleMap, false, 0, 0)
        }

        viewModel.locationHistory.observe(
            this,
            androidx.lifecycle.Observer {
                drawCoordinates(
                    viewModel.locationHistory.value!!,
                    googleMap
                )
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_location_history, container, false)
        viewModel = ViewModelProvider(this).get(LocationHistoryViewModel::class.java)
        AndroidThreeTen.init(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    /**
     * @param mMap: GoogleMap to work on
     * @param filter: enable filtering by timestamps
     * @param start: start for timestamp filtering
     * @param end: end for timestamp filtering
     * sends a request to the server from the LocationApiClient and writes resulting list of locations
     * into the respective live data
     */
    private fun getGeoJSONFromServer(mMap: GoogleMap, filter: Boolean, start: Long, end: Long) {
        GlobalScope.launch {
            var response: ArrayList<Location> =
                LocationApiClient.getPositionsFromServerForID(requireContext())
            requireActivity().runOnUiThread {
                if (filter) {
                    response = filterCoordinates(response, start, end)
                }
                if (response.isNotEmpty()) {
                    viewModel.locationHistory.value = response
                }
            }
        }
    }

    /**
     * @param coordinates: list of coordinates to be drawn
     * @param mMap: map to be drawn on
     * Draws the given list of coordinates onto the given map and connects them with polylines
     */
    private fun drawCoordinates(coordinates: ArrayList<Location>, mMap: GoogleMap) {
        var notDrawnBefore: Boolean = false
        for (x in 0..coordinates.size - 2) {
            val left = LatLng(coordinates[x].latitude, coordinates[x].longitude)
            val right = LatLng(coordinates[x + 1].latitude, coordinates[x + 1].longitude)
            if (checkCoordinateDistance(left, right)) {
                mMap.addPolyline(PolylineOptions().add(left, right).color(getNextColor()))
            }
        }
        val start = LatLng(coordinates[0].latitude, coordinates[0].longitude)
        val end = LatLng(
            coordinates[coordinates.size - 1].latitude,
            coordinates[coordinates.size - 1].longitude
        )
        mMap.addMarker(
            MarkerOptions().position(start).title("Start Of Tracking Period")
        )
        mMap.addMarker(MarkerOptions().position(end).title("Last Tracked Position"))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(end, 15f))
    }

    private fun checkCoordinateDistance(left: LatLng, right: LatLng): Boolean {
        val lon1 = Math.toRadians(left.longitude)
        val lat1 = Math.toRadians(left.latitude)
        val lon2 = Math.toRadians(right.longitude)
        val lat2 = Math.toRadians(right.latitude)

        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = sin(dlat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2.0)
        val c = 2 * asin(sqrt(a))

        val radius = 6371

        return (c * radius) <= distanceThreshold
    }

    private var currentColorIndex: Int = 0
    private val BLACK = -0x1000000
    private val GRAY = -0x777778
    private val RED = -0x10000
    private val GREEN = -0xff0100
    private val BLUE = -0xffff01
    private val YELLOW = -0x100
    private val CYAN = -0xff0001
    private val MAGENTA = -0xff01
    private val colorArray: ArrayList<Int> =
        arrayListOf(BLACK, GRAY, RED, GREEN, BLUE, YELLOW, CYAN, MAGENTA)

    private fun getNextColor(): Int {
        val color = colorArray[currentColorIndex]
        currentColorIndex += 1
        currentColorIndex %= 8
        return color
    }

    /**
     * @param coordinates: coordinate list to be filtered
     * @param start: start time in milliseconds
     * @param end: end time in milliseconds
     * @return given list of coordinates filtered to only contain entries with a timestamp between start and end + 24 hours
     */
    private fun filterCoordinates(
        coordinates: ArrayList<Location>,
        start: Long,
        end: Long
    ): ArrayList<Location> {
        //increase end time by 24 hours
        val newEnd = end + (24 * 60 * 60 * 1000)
        val filtered: ArrayList<Location> = ArrayList()
        for (x in coordinates) {
            if (x.time in (start + 1) until newEnd) {
                filtered.add(x)
            }
        }
        return filtered
    }
}