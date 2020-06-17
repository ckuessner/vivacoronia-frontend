package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object LocationApiClient : ApiBaseClient() {
    private const val TAG = "LocationClient"

    private fun getEndpoint(): String {
        return "${getBaseUrl()}/locations/${getUserId()}"
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
        val url = getEndpoint()

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