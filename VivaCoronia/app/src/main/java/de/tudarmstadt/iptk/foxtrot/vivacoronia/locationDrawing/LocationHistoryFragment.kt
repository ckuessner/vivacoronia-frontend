package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.jakewharton.threetenabp.AndroidThreeTen
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentLocationHistoryBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class LocationHistoryFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private lateinit var viewModel: LocationHistoryViewModel

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

        binding.datePickerBtn.setOnClickListener{
            picker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
        picker.addOnPositiveButtonClickListener {
            selectionStart = picker.selection?.first!!
            selectionEnd = picker.selection?.second!!
            googleMap.clear()
            getGeoJSONFromServer(googleMap, true, selectionStart, selectionEnd)
        }

        binding.reset.setOnClickListener{
            googleMap.clear()
            getGeoJSONFromServer(googleMap, false, 0, 0)
        }

        viewModel.locationHistory.observe(this, androidx.lifecycle.Observer { drawCoordinates(viewModel.locationHistory.value!!, googleMap) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_location_history, container, false)
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
            val response: JSONArray = LocationApiClient.getPositionsFromServer(requireContext())
            requireActivity().runOnUiThread {
                var coordinates = parseGeoJSON(response.toString())
                if (filter) {
                    coordinates = filterCoordinates(coordinates, start, end)
                }
                if (coordinates.isNotEmpty()) {
                    viewModel.locationHistory.value = coordinates
                }
            }
        }
    }

    /**
     * @param json: given json file as string
     * @return list of locations parsed from given JSONArray
     */
    private fun parseGeoJSON(json: String): ArrayList<Location>{
        val parser: Parser = Parser.default()
        val parsed: JsonArray<*> = parser.parse(StringBuilder(json)) as JsonArray<*>
        val loc = parsed["location"] as JsonArray<*>
        val timestamps = parsed["time"] as JsonArray<*>
        val coordinates = loc["coordinates"] as JsonArray<*>

        return createCoordinates(coordinates, timestamps)
    }

    /**
     * @param coordinates: JsonArray to parse coordinates from
     * @param timestamps: JsonArray to parse timestamps from
     * @return a list of locations with coordinates and their respective timestamps
     */
    private fun createCoordinates(coordinates: JsonArray<*>, timestamps: JsonArray<*>): ArrayList<Location>{
        val formatter = org.threeten.bp.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
        //val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val listOfCoordinates = ArrayList<Location>()
        for (x in 0 until coordinates.size) {
            var coordinateTime: Long = 0
            if (timestamps[x] != null) {
                coordinateTime = ZonedDateTime.parse(timestamps[x] as String, formatter)
                    .toInstant().toEpochMilli()
                //coordinateTime = formatter.parse(timestamps[x] as String).time
            }
            val latlong = coordinates[x] as JsonArray<*>
            val lat = latlong[1] as Double
            val long = latlong[0] as Double
            val location = Location(Context.LOCATION_SERVICE)
            location.time = coordinateTime
            location.latitude = lat
            location.longitude = long
            listOfCoordinates.add(location)
        }
        return listOfCoordinates
    }

    /**
     * @param coordinates: list of coordinates to be drawn
     * @param mMap: map to be drawn on
     * Draws the given list of coordinates onto the given map and connects them with polylines
     */
    private fun drawCoordinates(coordinates: ArrayList<Location>, mMap: GoogleMap) {
        for (x in 0..coordinates.size - 2) {
            val left = LatLng(coordinates[x].latitude, coordinates[x].longitude)
            val right =
                LatLng(coordinates[x + 1].latitude, coordinates[x + 1].longitude)
            mMap.addPolyline(PolylineOptions().add(left, right))
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

    /**
     * @param coordinates: coordinate list to be filtered
     * @param start: start time in milliseconds
     * @param end: end time in milliseconds
     * @return given list of coordinates filtered to only contain entries with a timestamp between start and end + 24 hours
     */
    private fun filterCoordinates(coordinates: ArrayList<Location>, start: Long, end: Long): ArrayList<Location> {
        //increase end time by 24 hours
        val newEnd = end + (24*60*60*1000)
        val filtered: ArrayList<Location> = ArrayList()
        for (x in coordinates) {
            if (x.time in (start + 1) until newEnd) {
                filtered.add(x)
            }
        }
        return filtered
    }
}