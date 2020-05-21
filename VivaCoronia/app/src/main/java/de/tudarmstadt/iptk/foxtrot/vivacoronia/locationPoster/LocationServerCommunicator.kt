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

        val queue = Volley.newRequestQueue(context)
        val url = "http://localhost:3000/locations/$userID/"

        // get information from LocationService
        val locationJSONArray = null

        val jsonArrayRequest = JsonArrayRequest(Request.Method.POST, url, locationJSONArray,
            Response.Listener {response ->
                try {
                    Log.println(1, "Volley Log", "Response: $response")
                }catch (e:Exception){
                    Log.println(1, "Volley Log", "Exception: $e")
                }
            },
            Response.ErrorListener {
                Log.println(1, "Volley Error", "Volley error: $it")
            }
        )


        queue.add(jsonArrayRequest)

        Toast.makeText(context, "Information was send to server successfully!", Toast.LENGTH_SHORT).show()
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