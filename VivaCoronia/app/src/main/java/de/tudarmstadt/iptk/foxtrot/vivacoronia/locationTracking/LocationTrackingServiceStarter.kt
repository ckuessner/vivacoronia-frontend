package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler

private val TAG = "LocationStarter"

//==============================================================================================
// methods for starting location tracking service
/**
 * since this method is called either from main activity after starting the app or after booting
 * it has to be distinguished between these two cases by using the fromMainActivity variable
 */
fun checkPermissionsAndStartTracking(context: Context, fromMainActivity: Boolean) {
    // application flow: check permission -> if false -> request permission
    // all permission granted so start the service
    if (PermissionHandler.checkLocationPermissions(
            context
        )
    ) {
        requestLocationService(context, createBackgroundLocationRequest(), fromMainActivity)
    }
    // permissions not granted so ask the user for it
    else {
        if (fromMainActivity) {
            Log.v(TAG, "requeste location permission")
            PermissionHandler.requestLocationPermissions(
                context as Activity
            )
        }
        else {
            Log.i(TAG, "could not start at init, because permissions where not granted -> User has to start app")
            Toast.makeText(context, "Please open the app and enable Location access.", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * creates a request to determine which services (gps, wifi, cellular) have to be enabled
 */
private fun createBackgroundLocationRequest(): LocationRequest? {
    // code (with a few changes) from https://developer.android.com/training/location/change-location-settings see apache 2.0 licence
    return LocationRequest.create()?.apply {
        interval = Constants.LOCATION_TRACKING_REQUEST_INTERVAL
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // can be changed to low power settings depending on what we need
    }
}

/**
 * requests the services specified by the locationrequest
 */
private fun requestLocationService(context: Context, locationRequest: LocationRequest?, fromMainActivity: Boolean) {
    // code (with a few changes) from https://developer.android.com/training/location/change-location-settings see apache 2.0 licence
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest!!)

    val client: SettingsClient = LocationServices.getSettingsClient(context)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

    // gps, wifi etc is enabled so location tracking can be started
    task.addOnSuccessListener { _ ->
        val intent = Intent(context, LocationTrackingService::class.java)
        Log.v(TAG, "start service")
        // version check
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // gps, wifi etc is not enabled, so location tracking cannot be started
    // instead gps, wifi has to be enabled
    if (fromMainActivity) {
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    Log.v(TAG, "couldnt create foreground task")
                    // opens a dialog which offers the user to enable gps
                    exception.startResolutionForResult(
                        context as Activity,
                        Constants.LOCATION_ACCESS_SETTINGS_REQUEST_CODE
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }
    else {
        task.addOnFailureListener { _ ->
            Toast.makeText(context, "Please open App to enable tracking", Toast.LENGTH_LONG).show()
        }
    }
}