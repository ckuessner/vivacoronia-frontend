package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class LocationServerCommunicator {

    companion object {

        private var TAG = "LocationSending"


        /**
         * method returns true if upload was successfull otherwise false
         */
        fun sendPositionsToServer(context: Context, userID: Int, data: List<DBLocation>){
            if (!checkPermissions(
                    context
                )
            ) {
                // Permission is not granted
                Toast.makeText(
                    context,
                    context.getString(R.string.location_upload_service_toast_no_internet),
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
                                .put(dataEntry.longitude)
                                .put(dataEntry.latitude)
                        )
                )
                locationJSONArray.put(locationJSONObject)
            }

            Log.i(TAG, locationJSONArray.toString())

            // convert into string
            val mRequestBody: String = locationJSONArray.toString()

            // build a single request
            val jsonStringRequest = object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    Log.i(TAG, response)
                    GlobalScope.launch {
                        // since upload was successfull delete the entries from the db
                        val db = AppDatabase.getDatabase(context)
                        db.coronaDao().deleteLocations(data)
                    }
                },
                Response.ErrorListener { error ->
                    Log.i(TAG, error.toString())
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return mRequestBody.toByteArray(Charsets.UTF_8)
                }
            }


            // add request to queue
            queue.add(jsonStringRequest)
        }

        fun sendCurrentPositionToServer(context: Context, userID: Int, data: DBLocation) {

            if (!checkPermissions(
                    context
                )
            ) {
                // Permission is not granted
                Toast.makeText(
                    context,
                    context.getString(R.string.location_upload_service_toast_no_internet),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // convert single point into an array of one element
            val array : ArrayList<DBLocation> = ArrayList()
            array.add(data)

            // call the array to server method
            sendPositionsToServer(
                context,
                userID,
                array
            )
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