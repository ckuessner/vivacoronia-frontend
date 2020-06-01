package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationPoster

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationPoster.LocationServerCommunicator

class LocationService {
<<<<<<< HEAD
    
=======
>>>>>>> 5228ebd63b8f356a18613bc92d703356fea57478

        var context: Context
        constructor(conContext: Context) {
            context = conContext
        }
        private var TAG = "Location poster"
        private lateinit var fusedLocationClient : FusedLocationProviderClient


        inner class MySuccessListener : OnSuccessListener<Location> {
            var cxt : Context

            constructor(con : Context) {
                cxt = con
            }

            lateinit var data : Constants.DataPoint

            override fun onSuccess(p0: Location?) {
                if (p0 != null) {
                    val longitude = p0.latitude
                    val latitude = p0.longitude
                    val time = p0.time
                    data = Constants.DataPoint(longitude, latitude, time)
                    // TODO: Get unique User ID
                    LocationServerCommunicator.sendCurrentPositionToServer(cxt, Constants().USER_ID, data)
                }
                else {
                    Log.v(TAG, "Location is Null")
                    Toast.makeText(
                        context,
                        "Could not get location data, check if your GPS is on.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            public fun getLocation() : Constants.DataPoint? {
                return data
            }

        }

        //TODO: Maybe check whether GPS is enabled at all???
        fun sendSingleLocation() {
            var list = MySuccessListener(context)

            // if permission for location access is not granted, try to get permission from user
            if(!checkLocationPermissions(context)){
                requestLocationPermissions(context as Activity)
                //TODO: What to do if permission still isn't granted? just terminate maybe?
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener(list)
            fusedLocationClient.lastLocation.addOnFailureListener { exp: Exception? ->
                Log.v(TAG,"Permission for location wasn't granted")
            }

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

        // check permission
        return ActivityCompat.
            checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }



<<<<<<< HEAD
}

=======
}
>>>>>>> 5228ebd63b8f356a18613bc92d703356fea57478
