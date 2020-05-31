package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationNotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationTrackingService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload.setupUploadAlarm

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val ZXING_CAMERA_PERMISSION = 1

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // notification channel should be created as soon as possible when the application starts
        LocationNotificationHelper.createLocationNotificationChannel(this)

        // setup upload alarm
        setupUploadAlarm(applicationContext)

        // tracking in onResume startet
        checkPermissionsAndStartTracking()

        val updateInfectionFab : View = findViewById(R.id.update_infection_fab)
        updateInfectionFab.setOnClickListener { view ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, Array(1) {Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION)
            else {
                val intent = Intent(this, UpdateInfectionActivity::class.java).apply {}
                startActivity(intent)
            }
        }
    }



    private fun checkPermissionsAndStartTracking() {
        // application flow: check permission -> if false -> request permission
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
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings see apache 2.0 licence
        val locationRequest = LocationRequest.create()?.apply {
            interval = Constants().LOCATION_TRACKING_REQUEST_INTERVAL
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // can be changed to low power settings depending on what we need
        }
        return locationRequest
    }

    /**
     * requests the services specified by the locationrequest
     */
    private fun requestLocationService(locationRequest: LocationRequest?){
        // code (with a few changes) from https://developer.android.com/training/location/change-location-settings see apache 2.0 licence
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
                    Log.v(TAG, "couldnt create foreground task")
                    // opens a dialog which offers the user to enable gps
                    exception.startResolutionForResult(this@MainActivity,
                        Constants().LOCATION_ACCESS_SETTINGS_REQUEST_CODE)
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
        Log.i(TAG, "onRequestPermissionResult")
        when (requestCode) {
            // handle location permission requests
            Constants().LOCATION_ACCESS_PERMISSION_REQUEST_CODE -> {
                // request permissions again if location permission not granted
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    Log.v(TAG, "Location Access Denied")

                    // not called after "Deny and dont ask again"
                    if (Build.VERSION.SDK_INT >= 23 && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(this, getText(R.string.main_activity_toast_permission_rationale), Toast.LENGTH_LONG).show()
                        PermissionHandler.requestLocationPermissions(this)
                    }
                    Toast.makeText(this, getText(R.string.main_activity_toast_location_permission_denied), Toast.LENGTH_LONG).show()
                }
                // permission was granted so start foreground service
                else {
                    Log.v(TAG, "start location service")
                    requestLocationService(createBackgroundLocationRequest())
                }
            }
            ZXING_CAMERA_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, UpdateInfectionActivity::class.java).apply {}
                    startActivity(intent)
                } else {
                    Toast.makeText(this,"Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
