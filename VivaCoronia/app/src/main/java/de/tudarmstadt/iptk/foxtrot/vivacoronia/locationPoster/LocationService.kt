package de.tudarmstadt.iptk.foxtrot.locationPoster

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants

class LocationService {
    

        var context: Context
        constructor(conContext: Context) {
            context = conContext
        }
        private var TAG = "Location poster"
        private lateinit var fusedLocationClient : FusedLocationProviderClient

        fun getSingleLocation() : Constants.dataPoint? {
            Log.v(TAG, "hallo")
            val returnVal: Constants.dataPoint? = null
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                run {
                    val longitude = location!!.longitude
                    val latitude = location!!.latitude
                    val time = location!!.time
                    val returnVal = Constants.dataPoint(longitude, latitude, time)
                    Log.v(
                        TAG,
                        time.toString() + " - " + longitude.toString() + " - " + latitude.toString()
                    )
                }
            }
            return returnVal
        }
    }
