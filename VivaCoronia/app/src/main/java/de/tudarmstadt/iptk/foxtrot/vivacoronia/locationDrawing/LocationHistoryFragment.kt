package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
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
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class LocationHistoryFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private lateinit var viewModel: LocationHistoryViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Double = 0.1
    //Polyline speed threshold in kilometers per hour
    private val speedThreshold = 1

    private val dayInMillis = 24 * 60 * 60 * 1000
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

        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
        builder.setTitleText("Select a tracking period")
        val picker = builder.build()

        val selectionStart = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000)
        val selectionEnd = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000 + dayInMillis)
        getGeoJSONFromServer(selectionStart, selectionEnd)

        binding.datePickerBtn.setOnClickListener {
            picker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
        picker.addOnPositiveButtonClickListener {
            val start = Date(picker.selection?.first!!)
            val end = Date(picker.selection?.second!! + dayInMillis)
            googleMap.clear()
            getGeoJSONFromServer(start, end)
        }

        binding.reset.setOnClickListener {
            googleMap.clear()
            val startOfDay = LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000
            getGeoJSONFromServer(Date(startOfDay), Date(startOfDay + dayInMillis))
        }

        viewModel.locationHistory.observe(
            this,
            androidx.lifecycle.Observer {
                drawCoordinates(
                    it,
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
     * @param start: start for timestamp filtering
     * @param end: end for timestamp filtering
     * sends a request to the server from the LocationApiClient and writes resulting list of locations
     * into the respective live data
     */
    private fun getGeoJSONFromServer(start: Date, end: Date) {
        GlobalScope.launch {
            val response: ArrayList<Location> =
                LocationApiClient.getPositionsFromServerForID(requireContext(), start, end)
            if (response.isNotEmpty()) {
                requireActivity().runOnUiThread {
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
        val colors = getColorArray(coordinates.size, Color.parseColor("#4169E1"), Color.parseColor("#FF0000"))
        for (currentCoordinateIndex in 0..coordinates.size - 2) {
            val left = LatLng(coordinates[currentCoordinateIndex].latitude, coordinates[currentCoordinateIndex].longitude)
            val right = LatLng(coordinates[currentCoordinateIndex + 1].latitude, coordinates[currentCoordinateIndex + 1].longitude)
            val leftTime = coordinates[currentCoordinateIndex].time
            val rightTime = coordinates[currentCoordinateIndex + 1].time
            if (isCoordinateDistanceLessOrEqualThanThreshold(left, right) && isPathSpeedMoreThanThreshold(leftTime, rightTime, left, right)) {
                mMap.addPolyline(PolylineOptions().add(left, right).color(colors[currentCoordinateIndex]))
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

    /**
     * @param start: location of starting point
     * @param end: location of end point
     * @return boolean whether the distance between start and end is smaller/equal than the given threshold
     */
    private fun isCoordinateDistanceLessOrEqualThanThreshold(start: LatLng, end: LatLng): Boolean {
        val distance = getCoordinateDistanceOnSphere(start, end)
        return distance <= distanceThreshold
    }

    /**
     * @param startTime: timestamp of starting point
     * @param endTime: timestamp of end point
     * @param startLocation: location of starting point
     * @param endLocation: location of end point
     * @return boolean whether the average speed between start and end is greater than the given threshold
     */
    private fun isPathSpeedMoreThanThreshold(startTime: Long, endTime: Long, startLocation: LatLng, endLocation: LatLng): Boolean {
        val distance = getCoordinateDistanceOnSphere(startLocation, endLocation)
        val timeDifference = (endTime - startTime) / (1000 * 60 * 60)
        val speed = distance / timeDifference
        return speed > speedThreshold
    }

    private fun getCoordinateDistanceOnSphere(
        startLocation: LatLng,
        endLocation: LatLng
    ): Double {
        val lon1 = Math.toRadians(startLocation.longitude)
        val lat1 = Math.toRadians(startLocation.latitude)
        val lon2 = Math.toRadians(endLocation.longitude)
        val lat2 = Math.toRadians(endLocation.latitude)

        //Haversine formula, determines the great-circle distance between two points on a sphere with given longitude and latitude
        val deltaLon = lon2 - lon1
        val deltaLat = lat2 - lat1
        val innerFormula =
            sin(deltaLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2.0)
        val outerFormula = 2 * asin(sqrt(innerFormula))

        //radius of the earth in kilometers
        val radius = 6371
        return outerFormula * radius
    }

    /**
     * @param amount: amount of lines to be drawn/colored
     * @param startColor: color to start with
     * @param endColor: color to end with
     * @return ArrayList of colors for each line to be drawn interpolated between startColor and endColor
     */
    private fun getColorArray(amount: Int, startColor: Int, endColor: Int): ArrayList<Int>{
        val fraction = 1f / amount
        val colorArray = ArrayList<Int>()
        for (i in 0 until amount){
            colorArray.add(ColorUtils.blendARGB(startColor, endColor, i * fraction))
        }
        return colorArray
    }
}