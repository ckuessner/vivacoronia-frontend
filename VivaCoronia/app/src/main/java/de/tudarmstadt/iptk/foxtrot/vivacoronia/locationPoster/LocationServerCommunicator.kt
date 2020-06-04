package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationPoster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import org.json.JSONArray
import org.json.JSONObject

class LocationServerCommunicator {

    companion object {

        private var TAG = "LocationSending"


        fun sendPositionsToServer(context: Context, userID: Int, data: Array<Constants.DataPoint>){
            if (!checkPermissions(context)) {
                // Permission is not granted
                Toast.makeText(
                    context,
                    "No permission on accessing the internet",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            Log.i(TAG, "in send several positions method")

            val queue = Volley.newRequestQueue(context)
            val baseUrl = Constants().SERVER_BASE_URL
            val url = "$baseUrl/locations/$userID"

            // data that will get posted on server
            val locationJSONArray = JSONArray()

            // build a json array with the data that will be send to db
            for(dataEntry in data){
                val locationJSONObject = JSONObject()
                locationJSONObject.put("time", dataEntry.time)
                locationJSONObject.put(
                    "location",
                    JSONObject()
                        .put("type", "Point")
                        .put(
                            "coordinates",
                            JSONArray()
                                .put(dataEntry.x)
                                .put(dataEntry.y)
                        )
                )
                locationJSONArray.put(locationJSONObject)
            }

            Log.i(TAG, locationJSONArray.toString())

            // convert into string
            val mRequestBody: String = locationJSONArray.toString()

            // build a single request
            val jsonStringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    Log.i(TAG, response)
                    Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                },
                Response.ErrorListener { error ->
                    Log.i(TAG, error.toString())
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return mRequestBody.toByteArray(Charsets.UTF_8)
                }
            };


            // add request to queue
            queue.add(jsonStringRequest)

        }

        fun sendCurrentPositionToServer(context: Context, userID: Int, data: Constants.DataPoint) {

            if (!checkPermissions(context)) {
                // Permission is not granted
                Toast.makeText(
                    context,
                    "No permission on accessing the internet",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            Log.i(TAG, "In location server method")

            // convert single point into an array of one element
            val array = arrayOf(data)

            // call the array to server method
            sendPositionsToServer(context, userID, array)
        }

        private fun checkPermissions(context: Context): Boolean {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                return false
            }

            return true
        }

    }
}