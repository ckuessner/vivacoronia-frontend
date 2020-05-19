package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionHandler {


    companion object{
        private val TAG = "PerminssionHandler"

        /**
         * requests permissions for location services
         */
        fun requestLocationPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE),
                LOCATION_ACCESS_PERMISSION_REQUEST_CODE
            )
        }

        fun checkLocationPermissions(context: Context) : Boolean {
            // check wheter location permission was granted
            // get permissions
            val coarseLocationPermitted = ActivityCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            val fineLocationPermitted = ActivityCompat.
            checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
            // check permission
            return coarseLocationPermitted && fineLocationPermitted
        }
    }



}