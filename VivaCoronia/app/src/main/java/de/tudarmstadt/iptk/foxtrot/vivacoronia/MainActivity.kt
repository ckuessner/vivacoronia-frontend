package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationNotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationTrackingService

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // notification channel should be created as soon as possible when the application starts
        LocationNotificationHelper.createLocationNotificationChannel(this)

        // start the tracking service with the start of the app
        checkPermissionsAndStartTracking()
    }



    private fun checkPermissionsAndStartTracking() {
        // TODO add in onResume method the start of the service, if the service isnt running
        // all permission granted so start the service
        if (PermissionHandler.checkLocationPermissions(this)) {
            requestLocationService(createBackgroundLocationRequest())
        }
        // permissions not granted so ask the user for it
        else {
            Log.v(TAG, "requeste location permission")
            PermissionHandler.requestLocationPermissions(this)
        }
    }

    /**
     * creates a request to determine which services (gps, wifi, cellular) have to be enabled
     */
    private fun createBackgroundLocationRequest() : LocationRequest? {
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings
        val locationRequest = LocationRequest.create()?.apply {
            interval = LOCATION_TRACKING_REQUEST_INTERVAL   // request all 5 minutes
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // can be changed to low power settings depending on what we need
        }
        return locationRequest
    }

    /**
     * requests the services specified by the locationrequest
     */
    private fun requestLocationService(locationRequest: LocationRequest?){
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        // gps, wifi etc is enabled so location tracking can be started
        task.addOnSuccessListener { locationSettingsResponse ->
            val intent = Intent(this, LocationTrackingService::class.java)
            Log.v(TAG, "start service")
            // version check
            if (Build.VERSION.SDK_INT >= 26){
                startForegroundService(intent)
            }
            else {
                startService(intent)
            }
        }

        // gps, wifi etc is not enabled, so location tracking cannot be started
        // instead gps, wifi has to be enabled
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    Log.v(TAG, "request gps")
                    // opens a dialog which offers the user to enable gps
                    exception.startResolutionForResult(this@MainActivity,
                        LOCATION_ACCESS_SETTINGS_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }

    /**
     * handles permission requests
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            // handle location permission requests
            LOCATION_ACCESS_PERMISSION_REQUEST_CODE -> {
                // request permissions again if location permission not granted
                if (grantResults.isNotEmpty() &&
                    (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                            grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                    Log.v(TAG, "request gps")
                    PermissionHandler.requestLocationPermissions(this)
                }
                else {
                    Log.v(TAG, "request location service")
                    requestLocationService(createBackgroundLocationRequest())
                }
            }
        }
    }
}
