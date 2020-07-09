package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object LocationApiClient : ApiBaseClient() {
    private const val TAG = "LocationClient"

    private fun getUserEndpoint(): String {
        return "${getEndpoint()}${getUserId()}"
    }

    private fun getEndpoint(): String{
        return "${getBaseUrl()}/locations/"
    }

    /**
     * Sends the tracked locations of the user to the server
     *
     * @return true, if upload successful
     */
    fun sendPositionsToServer(context: Context, locations: List<DBLocation>) {
        val requestQueue = getRequestQueue(context) ?: return

        // Create JSONArray for request body with location records in it
        val locationJSONArray =
            marshalLocationRecordArray(
                locations
            )

        // build a single request
        val url = getUserEndpoint()

        val jsonStringRequest =
            JSONArrayRequest(
                Request.Method.POST, url, locationJSONArray,
                Response.Listener { response ->
                    Log.i(
                        TAG,
                        "server response: $response"
                    )
                    GlobalScope.launch {
                        // since upload was successful, delete the entries from the db
                        val db = AppDatabase.getDatabase(context)
                        db.coronaDao().deleteLocations(locations)
                    }
                },
                Response.ErrorListener { error ->
                    error.printStackTrace()
                    Log.e(
                        TAG,
                        "upload failed: $error"
                    )
                }
            )

        // Add the upload request to the request queue
        Log.i(TAG, "uploading to $url: $locationJSONArray")
        requestQueue.add(jsonStringRequest)
    }

    /**
     * @param json: given json file as string
     * @return list of locations parsed from given JSONArray
     */
    private fun parseGeoJSONForOneID(json: String): ArrayList<Location>{
        val parser: Parser = Parser.default()
        val parsed: JsonArray<*> = parser.parse(StringBuilder(json)) as JsonArray<*>
        val loc = parsed["location"] as JsonArray<*>
        val timestamps = parsed["time"] as JsonArray<*>
        val coordinates = loc["coordinates"] as JsonArray<*>

        return createCoordinates(coordinates, timestamps)
    }

    /**
     * @param json: given json file as string
     * @return hashmap with userID as key and the corresponding locations as entries
     */
    private fun parseGeoJSONForMultipleID(json: String): HashMap<Int, ArrayList<Location>>{
        val parser: Parser = Parser.default()
        val parsed: JsonArray<*> = parser.parse(StringBuilder(json)) as JsonArray<*>
        val loc = parsed["location"] as JsonArray<*>
        val timestamps = parsed["time"] as JsonArray<*>
        val ids = parsed["userId"] as JsonArray<*>
        val coordinates = loc["coordinates"] as JsonArray<*>

        return createCoordinatesUserMap(coordinates, timestamps, ids)
    }

    /**
     * @param coordinates JsonArray to parse coordinates from
     * @param timestamps JsonArray to parse timestamps from
     * @param ids JsonArray to parse userIDs from
     * @return hashmap with userID as key and the corresponding locations as entries
     */
    private fun createCoordinatesUserMap(coordinates: JsonArray<*>, timestamps: JsonArray<*>, ids: JsonArray<*>): HashMap<Int, ArrayList<Location>>{
        val formatter = org.threeten.bp.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
        //val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val map = HashMap<Int, ArrayList<Location>>()
        for (currentCoordinateIndex in 0 until coordinates.size) {
            val currentID = ids[currentCoordinateIndex] as Int
            val location = createLocationWithTimestamp(
                timestamps,
                currentCoordinateIndex,
                formatter,
                coordinates
            )
            if(map.containsKey(currentID)){
                val list = map[currentID]
                list!!.add(location)
                map[currentID] = list
            }
            else{
                val list = ArrayList<Location>()
                list.add(location)
                map[currentID] = list
            }
        }
        return map
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
        for (currentCoordinateIndex in 0 until coordinates.size) {
            val location = createLocationWithTimestamp(
                timestamps,
                currentCoordinateIndex,
                formatter,
                coordinates
            )
            listOfCoordinates.add(location)
        }
        return listOfCoordinates
    }

    private fun createLocationWithTimestamp(
        timestamps: JsonArray<*>,
        currentCoordinateIndex: Int,
        formatter: DateTimeFormatter?,
        coordinates: JsonArray<*>
    ): Location {
        var coordinateTime: Long = 0
        if (timestamps[currentCoordinateIndex] != null) {
            coordinateTime =
                ZonedDateTime.parse(timestamps[currentCoordinateIndex] as String, formatter)
                    .toInstant().toEpochMilli()
        }
        val latlong = coordinates[currentCoordinateIndex] as JsonArray<*>
        val location = createLocation(latlong, coordinateTime)
        return location
    }

    private fun createLocation(
        latlong: JsonArray<*>,
        coordinateTime: Long
    ): Location {
        val lat = latlong[1] as Double
        val long = latlong[0] as Double
        val location = Location(Context.LOCATION_SERVICE)
        location.time = coordinateTime
        location.latitude = lat
        location.longitude = long
        return location
    }

    fun getPositionsFromServerForID(context: Context, startTime: Date, endTime: Date, onErrorCallback: ((error: VolleyError) -> Unit)): ArrayList<Location>{
        val requestQueue = getRequestQueue(context) ?: return ArrayList()
        val responseFuture = RequestFuture.newFuture<JSONArray>()
        val requestUrl = Uri.parse(getUserEndpoint()).buildUpon()
                .appendQueryParameter("start", startTime.toString())
                .appendQueryParameter("end", endTime.toString())
                .build().toString()
        val request = JsonArrayRequest(requestUrl, responseFuture, Response.ErrorListener { onErrorCallback(it) })
        requestQueue.add(request)
        return parseGeoJSONForOneID(responseFuture.get().toString())
    }

    fun getPositionsFromServer(context: Context, onErrorCallback: ((error: VolleyError) -> Unit)): HashMap<Int, ArrayList<Location>>{
        val requestQueue = getRequestQueue(context) ?: return HashMap()
        val responseFuture = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayRequest(getEndpoint(), responseFuture, Response.ErrorListener { onErrorCallback(it) })
        requestQueue.add(request)
        return parseGeoJSONForMultipleID(responseFuture.get().toString())
    }

    /**
     * Marshal the location record list to a JSONArray
     */
    private fun marshalLocationRecordArray(data: List<DBLocation>): JSONArray {
        return JSONArray(data.map { dataEntry ->
            JSONObject()
                .put("time", dataEntry.time)
                .put(
                    "location",
                    JSONObject()
                        .put("type", "Point")
                        .put(
                            "coordinates",
                            JSONArray()
                                .put(dataEntry.longitude)
                                .put(dataEntry.latitude)
                        )
                )
        })
    }

    private class JSONArrayRequest(
        method: Int,
        url: String,
        val jsonArray: JSONArray,
        req: Response.Listener<String>,
        error: Response.ErrorListener
    ) : StringRequest(method, url, req, error) {
        override fun getBodyContentType(): String = "application/json; charset=utf-8"
        override fun getBody(): ByteArray = jsonArray.toString().toByteArray(Charsets.UTF_8)
    }
}