package de.tudarmstadt.iptk.foxtrot.locationPoster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

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


        // TODO:


        //queue.add(jsonObjectRequest)
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