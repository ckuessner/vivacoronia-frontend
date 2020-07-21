package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionHandler {


    companion object{

        /**
         * requests permissions for location services
         */
        fun requestLocationPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants().LOCATION_ACCESS_PERMISSION_REQUEST_CODE
            )
        }

        /**
         * returns True if Location access granted, False if not
         */
        fun checkLocationPermissions(context: Context) : Boolean {
            // check whether location permission was granted
            // since fine location is more accurate than coarse, coarse is included in fine
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        fun checkCameraPermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }

        fun requestCameraPermissions(fragment: Fragment) {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                Constants().CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }
}