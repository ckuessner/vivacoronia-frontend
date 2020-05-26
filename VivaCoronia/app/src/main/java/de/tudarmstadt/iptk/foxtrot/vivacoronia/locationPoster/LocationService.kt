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
import com.google.android.gms.tasks.OnSuccessListener
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import javax.security.auth.callback.Callback

class LocationService {

        var context: Context
        constructor(conContext: Context) {
            context = conContext
        }
        private var TAG = "Location poster"
        private lateinit var fusedLocationClient : FusedLocationProviderClient


        inner class MySuccesListener : OnSuccessListener<Location>{
            var cxt : Context

            constructor(con : Context){
                cxt = con
            }

            lateinit var data : Constants.dataPoint
            override fun onSuccess(p0: Location?) {
                val longitude = p0!!.longitude
                val latitude = p0!!.latitude
                val time = p0!!.time
                data = Constants.dataPoint(longitude, latitude, time)
                LocationServerCommunicator.sendCurrentPositionToServer(cxt, 1234, data)
            }

            public fun getLocation() : Constants.dataPoint?{
                return data
            }

        }

        //TODO: Maybe check whether GPS is enabled at all???
        fun sendSingleLocation() : Constants.dataPoint? {
            var list = MySuccesListener(context)

            // if permission for location access is not granted, try to get permission from user
            if(!checkLocationPermissions(context)){
                requestLocationPermissions(context as Activity)
                //TODO: What to do if permission still isn't granted? just terminate maybe?
            }
            lateinit var returnVal : Constants.dataPoint
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener(list)
            fusedLocationClient.lastLocation.addOnFailureListener { exp: Exception? ->
                Log.v(TAG,"Permission for location wasn't granted")
            }

            returnVal = Constants.dataPoint(5.0, 5.0, 5)
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