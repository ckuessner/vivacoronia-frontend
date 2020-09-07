package de.tudarmstadt.iptk.foxtrot.vivacoronia.spreadMap

import android.app.AlertDialog
import android.graphics.Color
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.VolleyError
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.datepicker.MaterialDatePicker

import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.ContactApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSpreadMapBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.createCircleOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.generateColors
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getColorArray
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getLatLong
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.getZoomLevelForCircle
import de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions.GoogleMapFunctions.preprocessedCoordinatesForDrawing
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.LocationUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap

class SpreadMapFragment : Fragment() {

    private lateinit var binding: FragmentSpreadMapBinding
    private lateinit var viewModel: SpreadMapDataViewModel

    //Polyline length threshold in kilometers
    private val distanceThreshold: Float = 0.1F
    //Polyline speed threshold in kilometers per hour
    private val speedThreshold: Float = 1F
    //current center for circle drawing
    private var currentCenter: LatLng? = null
    private val dayInMillis = 24 * 60 * 60 * 1000

    var selectionStart = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000)
    var selectionEnd = Date(LocalDate.now().atStartOfDay().atZone(ZoneOffset.UTC).toEpochSecond() * 1000 + dayInMillis)

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

        //this reloactes user if he doesn't have permission
        RequestUtility.handleForbiddenFragment(requireActivity())

        googleMap.uiSettings.isMapToolbarEnabled = false
        binding.progressHorizontal.visibility = View.GONE
        //change in location data for spreadmap calls request for contact data
        viewModel.spreadMapData.observe(
            this,
            androidx.lifecycle.Observer {
                getContactsForIDs()
            })

        //change in contact data for spreadmap calls drawing of location data
        viewModel.contactData.observe(
            this,
            androidx.lifecycle.Observer {
                drawCoordinatesFromMap(
                    viewModel.spreadMapData.value!!,
                    it,
                    googleMap
                )
            }
        )

        currentCenter = LocationUtility.getLastKnownLocation(requireActivity()) ?: LatLng(49.877457, 8.654372)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCenter, 15F))

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
        builder.setTitleText("Select a tracking period")
        val picker = builder.build()

        drawCircle(googleMap, selectionStart, selectionEnd)

        binding.progressHorizontal.max = 100
        binding.datePickerBtn.setOnClickListener {
            picker.show(requireActivity().supportFragmentManager, "DATE_PICKER")
        }
        picker.addOnPositiveButtonClickListener {
            selectionStart = Date(picker.selection?.first!!)
            selectionEnd = Date(picker.selection?.second!! + dayInMillis)
            googleMap.clear()
            drawCircle(googleMap, selectionStart, selectionEnd)
        }
        googleMap.setOnMapLongClickListener { latLng ->
            currentCenter = latLng
            drawCircle(googleMap, selectionStart, selectionEnd)
        }

        binding.distanceText.text = getString(R.string.filter_radius_distance_text, binding.seekbar.progress)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.distanceText.text = getString(R.string.filter_radius_distance_text, progress)
                val minRadius = 100
                if(progress < minRadius){
                    seekBar!!.progress = minRadius
                }
                if(fromUser){
                    //logic to calculate text position to follow seekbar slider
                    val width = seekBar!!.width - seekBar.paddingLeft - seekBar.paddingRight
                    var thumbPos = width * (seekBar.progress / (seekBar.max.toFloat()))
                    if((thumbPos + binding.distanceText.width + 20) > screenWidth && thumbPos > binding.distanceText.x){
                        thumbPos = binding.distanceText.x
                    }
                    if(thumbPos - 20 < 0 && thumbPos < binding.distanceText.x){
                        thumbPos = binding.distanceText.x
                    }
                    binding.distanceText.x = thumbPos
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //Log.d("user", "user started dragging")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.distanceText.text = getString(R.string.filter_radius_distance_text, seekBar!!.progress)
                if(currentCenter != null) {
                    val alertBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Spreadmap")
                    alertBuilder.setMessage("Apply the new radius for the current center?")
                    alertBuilder.setPositiveButton("Confirm") { _, _ ->
                        drawCircle(googleMap, selectionStart, selectionEnd)
                    }
                    alertBuilder.setNegativeButton(android.R.string.cancel) { _, _ ->
                    }
                    val dialog: AlertDialog = alertBuilder.create()
                    dialog.show()
                }
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
    private fun getGeoJSONMapFromServer(location: LatLng, distance: Int, start: Date, end: Date) {
        binding.progressHorizontal.visibility = View.VISIBLE
        binding.progressHorizontal.isIndeterminate = true
        GlobalScope.launch {
            val response: MutableMap<String, List<Location>> =
                LocationApiClient.getPositionsFromServer(requireContext(), location, distance, start, end, ::onFetchErrorCallback)

            requireActivity().runOnUiThread {
                viewModel.spreadMapData.value = response
            }
        }
    }

    /**
     * after locations for spreadmap are retrieved from server, all contacts for all IDs
     * in the locations get requested from server
     */
    private fun getContactsForIDs(){
        GlobalScope.launch {
            val ids = viewModel.spreadMapData.value!!.keys.toList()
            val response: MutableMap<String, Pair<Boolean, ZonedDateTime>> =
                ContactApiClient.getContactsForIDsFromServer(ids, requireContext(), ::onFetchErrorCallback)

            requireActivity().runOnUiThread {
                viewModel.contactData.value = response
            }
        }
    }

    private fun onFetchErrorCallback(exception: VolleyError) {
        binding.progressHorizontal.visibility = View.GONE
        Log.e("SpreadMapFragment", "Error while fetching location data from server", exception)
    }

    /**
     * @param coordinatesMap: given map of userIDs as keys and location lists as values
     * @param mMap: given map to draw on
     * draws the routes for every given userID in the KeyValue-Map on the GoogleMap with randomized colors
     */
    private fun drawCoordinatesFromMap(coordinatesMap: MutableMap<String, List<Location>>, contacts: MutableMap<String, Pair<Boolean, ZonedDateTime>>?, mMap: GoogleMap){
        if(coordinatesMap.isEmpty()){
            binding.progressHorizontal.visibility = View.GONE
        }
        else{
            val processedLocationMap = getPreprocessedCoordinateMap(coordinatesMap)
            val colorPairsForIDs = generateColors(processedLocationMap.size)
            var currentColorPairForID = 0
            for((key, processedList) in processedLocationMap){
                //check if current ID has had a contact and when it was the infected, get timestamp of infection to filter with
                val recolorAfterTimestamp: Boolean = if(contacts != null && contacts.containsKey(key)) contacts[key]!!.first else false
                val timestampForRecoloring: Long? = if(contacts != null && contacts.containsKey(key)) contacts[key]!!.second.toInstant().toEpochMilli() else null
                val hasIDHadContact = contacts != null && contacts.containsKey(key)
                val colorFadeForID = getColorArray(processedList.flatten().size, colorPairsForIDs[currentColorPairForID].first, colorPairsForIDs[currentColorPairForID].second)  //create color fade for this IDs locations
                currentColorPairForID++ //counter for current color pair for each ID
                var currentColorInFadeIndex = 0 //counter for current color in color fade for this ID
                for (processedSubList in processedList) {
                    if (processedSubList.size == 1) { //create circle for single, unconnected location
                        var currentColor = colorFadeForID[currentColorInFadeIndex]
                        if(hasIDHadContact && timestampForRecoloring != null){
                            val locationTime = processedSubList[0].time
                            if(!recolorAfterTimestamp || timestampForRecoloring < locationTime){  //change color to red if location was recorded after a contact
                                currentColor = Color.RED
                            }
                        }
                        currentColorInFadeIndex++
                        val circleForSingleLocation = createCircleOptions(
                            processedSubList[0].getLatLong(),
                            currentColor
                        )
                        mMap.addCircle(circleForSingleLocation)
                    } else { //create polyline for multiple, connected locations
                        for (currentCoordinateIndex in 0..processedSubList.size - 2) {
                            val polylineStart = processedSubList[currentCoordinateIndex].getLatLong()
                            val polylineEnd = processedSubList[currentCoordinateIndex + 1].getLatLong()
                            var currentColor = colorFadeForID[currentColorInFadeIndex]
                            if(hasIDHadContact && timestampForRecoloring != null){
                                val locationTime = processedSubList[currentCoordinateIndex + 1].time
                                if(!recolorAfterTimestamp || timestampForRecoloring < locationTime){  //change color to red if location was recorded after a  contact
                                    currentColor = Color.RED
                                }
                            }
                            mMap.addPolyline(
                                PolylineOptions().add(polylineStart, polylineEnd).color(currentColor)
                            )
                            currentColorInFadeIndex++
                        }
                    }
                }
                val startMarkerLocation = processedList.first().first().getLatLong()
                val endMarkerLocation = processedList.last().last().getLatLong()
                val markerColor = FloatArray(3)
                ColorUtils.colorToHSL(colorFadeForID.last(), markerColor)
                mMap.addMarker(
                    MarkerOptions().position(startMarkerLocation).title("Start for ID: $key").icon(BitmapDescriptorFactory.defaultMarker(markerColor[0]))
                )
                mMap.addMarker(
                    MarkerOptions().position(endMarkerLocation).title("End for ID: $key").icon(BitmapDescriptorFactory.defaultMarker(markerColor[0]))
                )
            }
            binding.progressHorizontal.visibility = View.GONE
        }
    }

    /**
     * @param coordinatesMap: KeyValue-Map with userIDs as keys and location lists as values
     * @return KeyValue-Map with userIDs as keys and lists of location lists as values with each sublist representing a
     * part of the route not connected to the other parts
     */
    private fun getPreprocessedCoordinateMap(coordinatesMap: MutableMap<String, List<Location>>): Map<String, List<List<Location>>>{
        val returnMap: MutableMap<String, List<List<Location>>> = mutableMapOf()
        for ((id,coordinates) in coordinatesMap){
            returnMap[id] = preprocessedCoordinatesForDrawing(coordinates, speedThreshold, distanceThreshold)
        }
        val returnMapCopy = HashMap(returnMap)
        for ((id, _) in returnMap){
            if(viewModel.contactData.value != null && !viewModel.contactData.value!!.containsKey(id)){
                returnMapCopy.remove(id)
            }
        }
        return returnMapCopy
    }

    /**
     * @param googleMap: map to be drawn on
     * draws circle with current seekbar progress as radius and current center onto the map
     */
    private fun drawCircle(googleMap: GoogleMap, start: Date, end: Date) {
        googleMap.clear()
        googleMap.addCircle(
            CircleOptions()
                .center(currentCenter)
                .radius(binding.seekbar.progress.toDouble())
                .strokeColor(
                    Color.BLACK
                )
                .strokeWidth(3F)
        )
        getGeoJSONMapFromServer(currentCenter!!, binding.seekbar.progress, start, end)
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentCenter,
                getZoomLevelForCircle(binding.seekbar.progress)
            )
        )
    }
}