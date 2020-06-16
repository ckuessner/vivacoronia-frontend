package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.threetenabp.AndroidThreeTen
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import org.json.JSONArray
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class LocationDrawingService : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var mDatePickerBtn: FloatingActionButton
    private lateinit var mDateResetBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_drawing)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mDatePickerBtn = findViewById(R.id.date_picker_btn)
        mDateResetBtn = findViewById(R.id.reset)
        AndroidThreeTen.init(this)
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        var selectionStart: Long = now.timeInMillis
        var selectionEnd: Long = now.timeInMillis

        builder.setSelection(androidx.core.util.Pair(now.timeInMillis, now.timeInMillis))
        builder.setTitleText("Select a tracking period")
        val picker = builder.build()

        getGeoJSONFromServer(mMap, false, selectionStart, selectionEnd)

        mDatePickerBtn.setOnClickListener(fun(_: View) {
            picker.show(supportFragmentManager, "DATE_PICKER")
        })
        picker.addOnPositiveButtonClickListener {
            selectionStart = picker.selection?.first!!
            selectionEnd = picker.selection?.second!!
            mMap.clear()
            getGeoJSONFromServer(mMap, true, selectionStart, selectionEnd)
        }

        mDateResetBtn.setOnClickListener(fun(_: View){
            mMap.clear()
            getGeoJSONFromServer(mMap, false, 0, 0)
        })
    }

    private fun getUserID() = Constants().USER_ID

    private fun getServerURL() = Constants().SERVER_BASE_URL

    /**
     * Parses given JSONArray into an arrayList of Locations with coordinates and timestamp
     */
    private fun parseGeoJSON(json: JSONArray): ArrayList<Location> {
        val parser: Parser = Parser.default()
        val parsed: JsonArray<*> = parser.parse(StringBuilder(json.toString())) as JsonArray<*>
        val loc = parsed["location"] as JsonArray<*>
        val timestamps = parsed["time"] as JsonArray<*>
        val coordinates = loc["coordinates"] as JsonArray<*>

        return createCoordinates(coordinates, timestamps)
    }

    private fun createCoordinates(coordinates: JsonArray<*>, timestamps: JsonArray<*>): ArrayList<Location> {
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
            val lat = latlong[0] as Double
            val long = latlong[1] as Double
            val location = Location(Context.LOCATION_SERVICE)
            location.time = coordinateTime
            location.latitude = lat
            location.longitude = long
            listOfCoordinates.add(location)
        }
        return listOfCoordinates
    }

    /**
     * @param mMap: GoogleMap to work on
     * gets userID and server address, sends request to server to receive JSONArray
     * the array is then parsed into coordinates which are then drawn onto the given map
     * the first and last point are marked, all other points are connected with polylines
     */
    private fun getGeoJSONFromServer(mMap: GoogleMap, filter: Boolean, start: Long, end: Long) {
        val userURL = "${getServerURL()}/locations/${getUserID()}/"

        val queue = Volley.newRequestQueue(this)

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, userURL, null,
            Response.Listener { response ->
                var coordinates = parseGeoJSON(response)
                if (filter) {
                    coordinates = filterCoordinates(coordinates, start, end)
                }
                if (coordinates.isNotEmpty()) {
                    drawCoordinates(coordinates, mMap)
                }
            },
            Response.ErrorListener { error -> Log.d("RequestError", error.toString()) })

        queue.add(jsonArrayRequest)
    }

    private fun drawCoordinates(
        coordinates: ArrayList<Location>,
        mMap: GoogleMap
    ) {
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

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f))
    }

    private fun filterCoordinates(
        coordinates: ArrayList<Location>,
        start: Long,
        end: Long
    ): ArrayList<Location> {
        val filtered: ArrayList<Location> = ArrayList()
        for (x in coordinates) {
            if (x.time in (start + 1) until end) {
                filtered.add(x)
            }
        }
        return filtered
    }
}