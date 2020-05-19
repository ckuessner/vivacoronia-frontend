package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
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
        // TODO also invoke in onResume
        LocationNotificationHelper.createLocationNotificationChannel(this)

        checkPermissionsAndStartTracking()
    }



    fun checkPermissionsAndStartTracking() {
        // TODO add in onResume method the start of the service, if the service isnt running
        if (PermissionHandler.checkLocationPermissions(this)) {
            requestLocationService(createBackgroundLocationRequest())
        }
        else {
            Log.v(TAG, "requeste Standortzugriff")
            PermissionHandler.requestLocationPermissions(this)
        }
    }

    fun createBackgroundLocationRequest() : LocationRequest? {
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings
        val locationRequest = LocationRequest.create()?.apply {
            interval = 300000   // request all 5 minutes
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // can be changed to low power settings depending on what we need
        }
        return locationRequest
    }

    fun requestLocationService(locationRequest: LocationRequest?){
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        // gps, wifi etc is enabled so location tracking can be started
        task.addOnSuccessListener { locationSettingsResponse ->
            var intent: Intent = Intent(this, LocationTrackingService::class.java)
            Log.v(TAG, "startet foreground service")
            startForegroundService(intent)
        }

        // gps, wifi etc is not enabled, so location tracking cannot be startet
        // instead gps, wifi has to be enabled
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    Log.v(TAG, "requeste GPS")
                    exception.startResolutionForResult(this@MainActivity,
                        LOCATION_ACCESS_SETTINGS_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }

    fun requestSingleLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location: Location? ->  run{
            val longtitude = location!!.longitude
            val latitude = location!!.latitude
            val time = location!!.time
            Log.v(TAG, time.toString() + " - " + longtitude.toString() + " - " + latitude.toString())
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
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                    Log.v(TAG, "requeste Standortzugriff erneut")
                    PermissionHandler.requestLocationPermissions(this)
                }
                else {
                    Log.v(TAG, "request Location Background Service")
                    requestLocationService(createBackgroundLocationRequest())
                }
            }
        }
    }
}
