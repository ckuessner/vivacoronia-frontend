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
import org.json.JSONArray
import org.json.JSONObject

class LocationServerCommunicator {

    var context: Context
    var userID: Int

    constructor(context:Context, userID:Int) {
        this.context = context
        this.userID = userID
    }

    fun sendCurrentPositionToServer() {

        if (!checkPermissions()) {
            // Permission is not granted
            Toast.makeText(context, "No permission on accessing the internet", Toast.LENGTH_LONG).show()
            return
        }

        Log.i("bla", "In location server method")

        val queue = Volley.newRequestQueue(context)
        val url = "http://localhost:3000/locations/$userID/"

        // get information from LocationService
        val locationJSONArray = JSONArray()
        val locationJSONObject = JSONObject()

        val data = LocationService(context).getSingleLocation()

        locationJSONObject.put("time", data?.time.toString())

        locationJSONObject.put("location",
            JSONObject()
                .put("type", "Point")
                .put("coordinates",
                    JSONArray()
                        .put(data?.x)
                        .put(data?.y)
                )
        )

        locationJSONArray.put(locationJSONObject)



        Log.i("bla", locationJSONArray.toString())

        val mRequestBody:String = locationJSONArray.toString()

        val jsonStringRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->  Log.i("bla", response)},
            Response.ErrorListener { error ->  Log.i("bla", error.toString())}
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

    fun checkPermissions():Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false
        }

        return true
    }

}