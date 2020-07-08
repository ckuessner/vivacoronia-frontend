package de.tudarmstadt.iptk.foxtrot.vivacoronia.spreadMap

import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentLocationHistoryBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.math.*

class SpreadMapFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private lateinit var viewModel: SpreadMapDataViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Double = 0.1
    //Polyline speed threshold in kilometers per hour
    private val speedThreshold = 1

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
        getGeoJSONMapFromServer(LatLng(49.87167, 8.65027), 2000)
        viewModel.spreadMapData.observe(
            this,
            androidx.lifecycle.Observer {
                drawCoordinatesFromMap(
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
        viewModel = ViewModelProvider(this).get(SpreadMapDataViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    /**
     * @param location: center location around which to get coordinates from
     * @param distance: radius around center
     * sends a request to the server from the LocationApiClient and writes resulting map of ids with
     * their locations into the respective live data
     */
    private fun getGeoJSONMapFromServer(location: LatLng, distance: Int) {
        binding.progressHorizontal.visibility = View.VISIBLE
        binding.progressHorizontal.isIndeterminate = true
        GlobalScope.launch {
            val response: MutableMap<Int, List<Location>> =
                LocationApiClient.getPositionsFromServer(requireContext(), location, distance)

            requireActivity().runOnUiThread {
                viewModel.spreadMapData.value = response
            }
        }
    }

    private fun drawCoordinatesFromMap(coordinatesMap: MutableMap<Int, List<Location>>, mMap: GoogleMap){
        if(coordinatesMap.isEmpty()){
            binding.progressHorizontal.visibility = View.GONE
        }
        else{
            val processedMap = getPreprocessedCoordinateMap(coordinatesMap)
            var elementCount = 0
            for((key,list) in coordinatesMap) {
                elementCount += list.size
            }
            val colors = getColorArray(
                elementCount,
                Color.parseColor("#4169E1"),
                Color.parseColor("#FF0000"))
            var currentColorIndex = 0
            for((key, processedList) in processedMap){
                for (processedSubList in processedList){
                    if (processedSubList.size == 1) {
                        val currentColor = colors[currentColorIndex]
                        currentColorIndex++
                        val circleOptions = createCircleOptions(
                            processedSubList[0].getLatLong(),
                            currentColor
                        )
                        mMap.addCircle(circleOptions)
                    } else {
                        for (currentCoordinateIndex in 0..processedSubList.size - 2) {
                            val left = processedSubList[currentCoordinateIndex].getLatLong()
                            val right = processedSubList[currentCoordinateIndex + 1].getLatLong()
                            mMap.addPolyline(
                                PolylineOptions().add(left, right).color(colors[currentColorIndex])
                            )
                            currentColorIndex++
                        }
                    }
                    val startMarkerLocation = processedSubList[0].getLatLong()
                    val endMarkerLocation = processedSubList[processedSubList.size - 1].getLatLong()
                    mMap.addMarker(
                        MarkerOptions().position(startMarkerLocation).title("Start for ID: $key")
                    )
                    mMap.addMarker(
                        MarkerOptions().position(endMarkerLocation).title("End for ID: $key")
                    )

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endMarkerLocation, 15f))
                }
            }
            binding.progressHorizontal.visibility = View.GONE
        }
    }

    /**
     * @param center: coordinates of center point
     * @param currentColor: current color from color array
     * @return Circle options to be drawn onto the map
     */
    private fun createCircleOptions(
        center: LatLng,
        currentColor: Int
    ): CircleOptions {
        val circleOptions = CircleOptions()
        circleOptions.center(center)
        circleOptions.radius(2.0)
        circleOptions.strokeColor(currentColor)
        circleOptions.fillColor(currentColor)
        circleOptions.strokeWidth(2f)
        return circleOptions
    }

    /**
     * @param amount: amount of lines to be drawn/colored
     * @param startColor: color to start with
     * @param endColor: color to end with
     * @return ArrayList of colors for each line to be drawn interpolated between startColor and endColor
     */
    private fun getColorArray(amount: Int, startColor: Int, endColor: Int): List<Int>{
        val fraction = 1f / amount
        val colorArray = ArrayList<Int>()
        for (i in 0 until amount){
            colorArray.add(ColorUtils.blendARGB(startColor, endColor, i * fraction))
        }
        return colorArray
    }

    private fun getPreprocessedCoordinateMap(coordinatesMap: MutableMap<Int, List<Location>>): Map<Int, List<List<Location>>>{
        val returnMap: MutableMap<Int, List<List<Location>>> = mutableMapOf()
        for ((id,coordinates) in coordinatesMap){
            returnMap[id] = preprocessedCoordinatesForDrawing(coordinates)
        }
        return returnMap
    }

    /**
     * @param coordinates: list of unprocessed coordinates
     * @return list of lists containing coordinates which are closer to each other than the distance threshold and
     * the speed between them is greater than the speed threshold
     */
    private fun preprocessedCoordinatesForDrawing(coordinates: List<Location>): List<List<Location>>{
        val returnList = ArrayList<ArrayList<Location>>()
        for (coordinate in coordinates){
            when {
                returnList.isEmpty() -> {
                    returnList.add(arrayListOf(coordinate))
                }
                checkSpeedAndDistance(returnList.last().last(), coordinate) -> {
                    returnList.last().add(coordinate)
                }
                else -> {
                    returnList.add(arrayListOf(coordinate))
                }
            }
        }
        return returnList
    }

    /**
     * @param start: location of starting point
     * @param end: location of end point
     * @return boolean whether the two points are closer to each other than the distance threshold and
     * the speed between them is greater than the speed threshold
     */
    private fun checkSpeedAndDistance(start: Location, end: Location): Boolean {
        val startLatLng = start.getLatLong()
        val endLatLng = end.getLatLong()
        val isTimeRelevant = isSpeedOnPathGreaterThanThreshold(start.time, end.time, startLatLng, endLatLng)
        val isDistanceRelevant = isCoordinateDistanceLessOrEqualThanThreshold(startLatLng, endLatLng)
        return isTimeRelevant && isDistanceRelevant
    }

    /**
     * @return LatLng containing the latitude and longitude of the given location
     */
    private fun Location.getLatLong(): LatLng {
        val lat = this.latitude
        val long = this.longitude
        return LatLng(lat, long)
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
    private fun isSpeedOnPathGreaterThanThreshold(startTime: Long, endTime: Long, startLocation: LatLng, endLocation: LatLng): Boolean {
        val distance = getCoordinateDistanceOnSphere(startLocation, endLocation)
        val timeDifference = (endTime - startTime) / (1000 * 60 * 60)
        val speed = distance / timeDifference
        return speed > speedThreshold
    }

    /**
     * @param startLocation: location of starting point
     * @param endLocation: location of end point
     * @return the distance between the two given locations on a sphere with the size of earth
     */
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
}