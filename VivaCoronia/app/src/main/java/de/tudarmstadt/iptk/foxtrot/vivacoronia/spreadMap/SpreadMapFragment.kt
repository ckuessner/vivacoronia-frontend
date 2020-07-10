package de.tudarmstadt.iptk.foxtrot.vivacoronia.spreadMap

import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.VolleyError
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSpreadMapBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.createCircleOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.generateColors
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getColorArray
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getLatLong
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.preprocessedCoordinatesForDrawing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SpreadMapFragment : Fragment() {

    private lateinit var binding: FragmentSpreadMapBinding
    private lateinit var viewModel: SpreadMapDataViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Float = 0.1F
    //Polyline speed threshold in kilometers per hour
    private val speedThreshold: Float = 1F

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
        //TODO: set initial call location if deemed useful
        //getGeoJSONMapFromServer(LatLng(49.87167, 8.65027), 2000)
        viewModel.spreadMapData.observe(
            this,
            androidx.lifecycle.Observer {
                drawCoordinatesFromMap(
                    it,
                    googleMap
                )
            })
        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.clear()
            getGeoJSONMapFromServer(latLng, binding.seekbar.progress)
        }
        binding.distanceText.text = getString(R.string.filter_radius_distance_text, binding.seekbar.progress)
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.distanceText.text = getString(R.string.filter_radius_distance_text, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.d("user", "user started dragging")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("user", "user stopped dragging")
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_spread_map, container, false)
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
                LocationApiClient.getPositionsFromServer(requireContext(), location, distance, ::onFetchErrorCallback)

            requireActivity().runOnUiThread {
                viewModel.spreadMapData.value = response
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
            Log.e("SpreadMapFragment", "Error while fetching location data from server", exception)
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
            val idColors = generateColors(processedMap.size)
            var currentIdColor = 0
            for((key, processedList) in processedMap){
                val colors = getColorArray(processedList.flatten().size, idColors[currentIdColor][0], idColors[currentIdColor][1])
                currentIdColor++
                var currentColorIndex = 0
                for (processedSubList in processedList) {
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
                }
                val startMarkerLocation = processedList.first().first().getLatLong()
                val endMarkerLocation = processedList.last().last().getLatLong()
                mMap.addMarker(
                    MarkerOptions().position(startMarkerLocation).title("Start for ID: $key")
                )
                mMap.addMarker(
                    MarkerOptions().position(endMarkerLocation).title("End for ID: $key")
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endMarkerLocation, 15f))
            }
            binding.progressHorizontal.visibility = View.GONE
        }
    }

    private fun getPreprocessedCoordinateMap(coordinatesMap: MutableMap<Int, List<Location>>): Map<Int, List<List<Location>>>{
        val returnMap: MutableMap<Int, List<List<Location>>> = mutableMapOf()
        for ((id,coordinates) in coordinatesMap){
            returnMap[id] = preprocessedCoordinatesForDrawing(coordinates, speedThreshold, distanceThreshold)
        }
        return returnMap
    }
}