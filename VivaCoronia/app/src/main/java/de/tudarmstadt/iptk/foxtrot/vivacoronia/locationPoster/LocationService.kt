package de.tudarmstadt.iptk.foxtrot.locationPoster

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
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

            // while permission for location access is not granted, get it
            if(!checkLocationPermissions(context)){
                requestLocationPermissions(context as Activity)
            }

            var returnVal: Constants.dataPoint? = null
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                run {
                    val longitude = location!!.longitude
                    val latitude = location!!.latitude
                    val time = location!!.time
                    returnVal = Constants.dataPoint(longitude, latitude, time)
                    Log.v(
                        TAG,
                        time.toString() + " - " + longitude.toString() + " - " + latitude.toString()
                    )
                }
            }

            fusedLocationClient.lastLocation.addOnFailureListener { exp: Exception? ->
                Log.v(TAG,"Permission for location wasn't granted")
            }

            return returnVal
        }
    
    private fun requestLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            56
        )// 56 should be the constant for LOCATION_ACCESS_PERMISSION_REQUEST_CODE, but it doesnt work
    }

    /**
     * returns True if Location access granted, False if not
     */
    private fun checkLocationPermissions(context: Context) : Boolean {
        // check wheter location permission was granted
        // since fine location is more accurate than coarse, coarse is included in fine
        val fineLocationPermitted = ActivityCompat.
        checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        // check permission
        return fineLocationPermitted
    }



}

