package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.NotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.createBackgroundLocationRequest
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.requestLocationService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload.setupUploadAlarm
import de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons.WebSocketService

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navView: NavigationView

    private fun checkAdminJWT(ctx : Context) : Boolean{
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        val adminJWT = settings.getString(Constants.adminJWT, null)
        return adminJWT != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // notification channel should be created as soon as possible when the application starts
        if (Build.VERSION.SDK_INT >= 24) { // importance needs api 24
            NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.location_service_channel_name),
                getString(R.string.location_service_channel_description),
                NotificationManager.IMPORTANCE_DEFAULT,
                Constants.LOCATION_NOTIFICATION_CHANNEL_ID
            )
            NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.infected_notification_channel_name),
                getString(R.string.infected_notification_channel_description),
                NotificationManager.IMPORTANCE_HIGH,
                Constants.INFECTED_NOTIFICATION_CHANNEL_ID
            )
        }

        // setup navigation
        val navController = findNavController(R.id.nav_fragment)
        navController.setGraph(R.navigation.nav_graph)
        // the location history view, trading view and achievements view are all root views
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.locationHistoryFragment,
                R.id.tradingOverviewFragment,
                R.id.achievementsFragment,
                R.id.infectionStatusFragment,
                R.id.spreadMapFragment
            ),
            findViewById<DrawerLayout>(R.id.drawer_layout)
        )

        // setup default toolbar with navcontroller
        // changes need to made here if a custom toolbar shall be used
        setupActionBarWithNavController(navController, appBarConfiguration)


        // setup nav view
        navView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)

        // add actions for drawer menu item here
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_location_history -> {
                    navController.navigate(R.id.locationHistoryFragment)
                }
                R.id.menu_item_infection_update -> {
                    navController.navigate(R.id.infectionStatusFragment)
                }
                R.id.menu_item_trading -> {
                    navController.navigate(R.id.tradingOverviewFragment)
                }
                R.id.menu_item_achievements -> {
                    navController.navigate(R.id.achievementsFragment)
                }
                R.id.menu_item_spreadmap -> {
                    navController.navigate(R.id.spreadMapFragment)
                }
            }
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(navView)
            return@setNavigationItemSelectedListener true
        }


    }

    override fun onResume() {
        super.onResume()
        // setup upload alarm
        setupUploadAlarm(applicationContext)

        //start websocket to listen for push notifications
        val websocketIntent = Intent(this, WebSocketService::class.java)
        startService(websocketIntent)

        // start tracking
        de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.checkPermissionsAndStartTracking(
            this,
            true
        )
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
            return when (item.itemId) {
                // the drawer button is pressed so close the drawer
                android.R.id.home -> {
                    if (findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(navView)) {
                        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(navView)
                    } else {
                        findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(navView)
                    }
                    true
                }
                else -> false
            }
        }
        return false
    }

    /**
     * handles permission requests
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        when (requestCode) {
            // handle location permission requests
            Constants.LOCATION_ACCESS_PERMISSION_REQUEST_CODE -> {
                // request permissions again if location permission not granted
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    Log.v(TAG, "Location Access Denied")

                    // not called after "Deny and dont ask again"
                    if (Build.VERSION.SDK_INT >= 23 && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(
                            this,
                            getText(R.string.main_activity_toast_permission_rationale),
                            Toast.LENGTH_LONG
                        ).show()
                        PermissionHandler.requestLocationPermissions(
                            this
                        )
                    }
                    Toast.makeText(
                        this,
                        getText(R.string.main_activity_toast_location_permission_denied),
                        Toast.LENGTH_LONG
                    ).show()
                }
                // permission was granted so start foreground service
                else {
                    Log.v(TAG, "start location service")
                    requestLocationService(this, createBackgroundLocationRequest(), true)
                }
            }
        }
    }
}

