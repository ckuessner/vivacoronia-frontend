package de.tudarmstadt.iptk.foxtrot.locationPoster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
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

        locationJSONObject.put("time", "2020-05-21T21:39:08+02:00")

        locationJSONObject.put("location",
            JSONObject()
                .put("type", "Point")
                .put("coordinates",
                    JSONArray()
                        .put(-80.1347334)
                        .put(25.7663562)
                )
        )

        locationJSONArray.put(locationJSONObject)



        Log.i("bla", locationJSONArray.toString())

        val jsonArrayRequest = JsonArrayRequest(Request.Method.POST, url, locationJSONArray,
            Response.Listener {response ->
                try {
                    Log.i("bla", "Response: $response")
                }catch (e:Exception){
                    Log.i("bla", "Exception: $e")
                }
            },
            Response.ErrorListener {
                Log.i("bla", "Volley error: $it")
            }
        )


        queue.add(jsonArrayRequest)

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