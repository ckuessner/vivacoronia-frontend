package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationNotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.LocationTrackingService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload.setupUploadAlarm
import android.widget.Button
import de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus.InfectionStatusActivity

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api

    private lateinit var appBarConfiguration : AppBarConfiguration

    private lateinit var navView : NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // notification channel should be created as soon as possible when the application starts
        LocationNotificationHelper.createLocationNotificationChannel(this)

        // setup upload alarm
        setupUploadAlarm(applicationContext)

        // tracking in onResume startet
        checkPermissionsAndStartTracking()

        // setup navigation
        val navController = findNavController(R.id.nav_fragment)
        navController.setGraph(R.navigation.nav_graph)

        // the location history view, trading view and achievements view are all root views
        appBarConfiguration = AppBarConfiguration(setOf(R.id.locationHistoryFragment,
            R.id.tradingOverviewFragment,
            R.id.achievementsFragment),
            findViewById<DrawerLayout>(R.id.drawer_layout))


        // setup default toolbar with navcontroller
        // changes need to made here if a custom toolbar shall be used
        setupActionBarWithNavController(navController, appBarConfiguration)


        // setup nav view
        navView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)

        // add actions for drawer menu item here
        navView.setNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_item_location_history -> {
                    navController.navigate(R.id.locationHistoryFragment)
                    return@setNavigationItemSelectedListener true
                }
                R.id.menu_item_infection_update -> {
                    Log.i(TAG, "clicked infection update")
                    val intent = Intent(this, InfectionStatusActivity::class.java).apply {}
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                R.id.menu_item_trading -> {
                    navController.navigate(R.id.tradingOverviewFragment)
                    return@setNavigationItemSelectedListener true
                }
                R.id.menu_item_achievements -> {
                    navController.navigate(R.id.achievementsFragment)
                    return@setNavigationItemSelectedListener true}
                else -> return@setNavigationItemSelectedListener false
            }
        }

    }

    //==============================================================================================
    // methods for drawer menu
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // needed to close the drawer with a click on the drawer icon
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when(item.itemId){
                // the drawer button is pressed so close the drawer
                android.R.id.home -> {
                    if (findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(navView)) {
                        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(navView)
                    }
                    else {
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(navView)
                    }
                    return true
                }
                else -> return false
            }
        }
        return false
    }


    //==============================================================================================
    // methods for starting location tracking service
    private fun checkPermissionsAndStartTracking() {
        // application flow: check permission -> if false -> request permission
        // TODO add in onResume method the start of the service, if the service isnt running
        // all permission granted so start the service
        if (PermissionHandler.checkLocationPermissions(
                this
            )
        ) {
            requestLocationService(createBackgroundLocationRequest())
        }
        // permissions not granted so ask the user for it
        else {
            Log.v(TAG, "requeste location permission")
            PermissionHandler.requestLocationPermissions(
                this
            )
        }
    }

    /**
     * creates a request to determine which services (gps, wifi, cellular) have to be enabled
     */
    private fun createBackgroundLocationRequest(): LocationRequest? {
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
    private fun requestLocationService(locationRequest: LocationRequest?) {
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
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        // gps, wifi etc is not enabled, so location tracking cannot be started
        // instead gps, wifi has to be enabled
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    Log.v(TAG, "couldnt create foreground task")
                    // opens a dialog which offers the user to enable gps
                    exception.startResolutionForResult(
                        this@MainActivity,
                        Constants().LOCATION_ACCESS_SETTINGS_REQUEST_CODE
                    )
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
                        PermissionHandler.requestLocationPermissions(
                            this
                        )
                    }
                    Toast.makeText(this, getText(R.string.main_activity_toast_location_permission_denied), Toast.LENGTH_LONG).show()
                }
                // permission was granted so start foreground service
                else {
                    Log.v(TAG, "start location service")
                    requestLocationService(createBackgroundLocationRequest())
                }
            }
        }
    }
}
