package de.tudarmstadt.iptk.foxtrot.locationPoster

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

        fun sendCurrentPositionToServer(context: Context, userID: Int, data: Constants.dataPoint) {

            if (!checkPermissions(context)) {
                // Permission is not granted
                Toast.makeText(
                    context,
                    "No permission on accessing the internet",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            Log.i("bla", "In location server method")

            val queue = Volley.newRequestQueue(context)
            val url = "http://192.168.2.105:3000/locations/$userID/"

            // get information from LocationService
            val locationJSONArray = JSONArray()
            val locationJSONObject = JSONObject()


            locationJSONObject.put("time", data?.time.toString())

            locationJSONObject.put(
                "location",
                JSONObject()
                    .put("type", "Point")
                    .put(
                        "coordinates",
                        JSONArray()
                            .put(data?.x)
                            .put(data?.y)
                    )
            )

            locationJSONArray.put(locationJSONObject)



            Log.i("bla", locationJSONArray.toString())

            val mRequestBody: String = locationJSONArray.toString()

            val jsonStringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response -> Log.i("bla", response) },
                Response.ErrorListener { error -> Log.i("bla", error.toString()) }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return mRequestBody.toByteArray(Charsets.UTF_8)
                }
            };


            queue.add(jsonStringRequest)

            var message = "Information was send to server successfully!"

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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