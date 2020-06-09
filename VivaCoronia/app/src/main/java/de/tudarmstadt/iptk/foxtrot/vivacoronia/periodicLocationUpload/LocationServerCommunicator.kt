package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object LocationServerCommunicator {
    private const val TAG = "LocationSending"

    /**
     * Sends the tracked locations of the user to the server
     *
     * @return true, if upload successful
     */
    fun sendPositionsToServer(context: Context, userID: Int, locations: List<DBLocation>) {
        if (!checkInternetPermissions(context)) {
            // Permission is not granted
            Toast.makeText(
                context,
                context.getString(R.string.location_upload_service_toast_no_internet),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Create JSONArray for request body with location records in it
        val locationJSONArray = marshalLocationRecordArray(locations)

        // build a single request
        val baseUrl = Constants().SERVER_BASE_URL
        val url = "$baseUrl/locations/$userID"

        val jsonStringRequest = JSONArrayRequest(
            Request.Method.POST, url, locationJSONArray,
            Response.Listener { response ->
                Log.i(TAG, "server response: $response")
                GlobalScope.launch {
                    // since upload was successful, delete the entries from the db
                    val db = AppDatabase.getDatabase(context)
                    db.coronaDao().deleteLocations(locations)
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "upload failed: $error")
            }
        )

        // Add the upload request to the request queue
        Log.i(TAG, "uploading to $url: $locationJSONArray")
        getRequestQueue(context)
            .add(jsonStringRequest)
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

    private fun getRequestQueue(context: Context): RequestQueue {
        return Volley.newRequestQueue(context)
    }

    private fun checkInternetPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
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