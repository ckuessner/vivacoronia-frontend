package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.VolleyError
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.datepicker.MaterialDatePicker
import com.jakewharton.threetenabp.AndroidThreeTen
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentLocationHistoryBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.createCircleOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getColorArray
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getLatLong
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.preprocessedCoordinatesForDrawing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.util.*


private const val TAG = "LocationHistoryFragment"

class LocationHistoryFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private lateinit var viewModel: LocationHistoryViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Float = 0.1F
    //Polyline speed threshold in kilometers per hour
    private val speedThreshold: Float = 1F

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
        var startLocation = LatLng(0.0, 0.0)
        if (PermissionHandler.checkLocationPermissions(requireActivity())) {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            @SuppressLint("MissingPermission") // Check is in PermissionHandler
            var currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            @SuppressLint("MissingPermission")
            if(currentLocation == null){
                currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            if (currentLocation != null)
                startLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15F))

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
        builder.setTitleText("Select a tracking period")
        val picker = builder.build()

        val selectionStart = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000)
        val selectionEnd = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000 + dayInMillis)
        getGeoJSONFromServer(selectionStart, selectionEnd)

        binding.progressHorizontal.max = 100
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
        binding.progressHorizontal.visibility = View.VISIBLE
        binding.progressHorizontal.isIndeterminate = true
        GlobalScope.launch {
            val response: List<Location> =
                LocationApiClient.getPositionsFromServerForID(requireContext(), start, end, ::onFetchErrorCallback)

            requireActivity().runOnUiThread {
                viewModel.locationHistory.value = response
            }
        }
    }

    private fun onFetchErrorCallback(exception: VolleyError) {
        binding.progressHorizontal.visibility = View.GONE
        if (requireActivity().hasWindowFocus())
                Toast.makeText(
                    requireActivity(),
                    "Failed to connect to server",
                    Toast.LENGTH_LONG
                ).show()
        else {
            Log.e(TAG, "Error while fetching location data from server", exception)
        }
    }

    /**
     * @param coordinates: list of coordinates to be drawn
     * @param mMap: map to be drawn on
     * Draws the given list of coordinates onto the given map and connects them with polylines
     */
    private fun drawCoordinates(coordinates: List<Location>, mMap: GoogleMap) {
        if(coordinates.isEmpty()){
            binding.progressHorizontal.visibility = View.GONE
        }
        else {
            val colors = getColorArray(
                coordinates.size,
                Color.parseColor("#4169E1"),
                Color.parseColor("#FF0000")
            )
            var currentColorIndex = 0
            val processedCoordinates = preprocessedCoordinatesForDrawing(coordinates, speedThreshold, distanceThreshold)
            for (currentCoordinateSubList in processedCoordinates) {
                if (currentCoordinateSubList.size == 1) {
                    val currentColor = colors[currentColorIndex]
                    currentColorIndex++
                    val circleOptions = createCircleOptions(
                        currentCoordinateSubList[0].getLatLong(),
                        currentColor
                    )
                    mMap.addCircle(circleOptions)
                } else {
                    for (currentCoordinateIndex in 0..currentCoordinateSubList.size - 2) {
                        val left = currentCoordinateSubList[currentCoordinateIndex].getLatLong()
                        val right = currentCoordinateSubList[currentCoordinateIndex + 1].getLatLong()
                        mMap.addPolyline(
                            PolylineOptions().add(left, right).color(colors[currentColorIndex])
                        )
                        currentColorIndex++
                    }
                }
            }

            val startMarkerLocation = coordinates[0].getLatLong()
            val endMarkerLocation = coordinates[coordinates.size - 1].getLatLong()
            mMap.addMarker(
                MarkerOptions().position(startMarkerLocation).title("Start Of Tracking Period")
            )
            mMap.addMarker(
                MarkerOptions().position(endMarkerLocation).title("Last Tracked Position")
            )

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endMarkerLocation, 15f))
            binding.progressHorizontal.visibility = View.GONE
        }
    }
}