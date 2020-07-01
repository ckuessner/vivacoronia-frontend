package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons.MyWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {
    private var TAG = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api

    private lateinit var appBarConfiguration : AppBarConfiguration

    private lateinit var navView : NavigationView

    private lateinit var client : OkHttpClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebSocket()

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
            R.id.achievementsFragment,
            R.id.infectionStatusFragment),
            findViewById(R.id.drawer_layout))


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
            }
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(navView)
            return@setNavigationItemSelectedListener true
        }

        val webButton = findViewById<Button>(R.id.websocket)
        webButton.setOnClickListener( {v -> initWebSocket()})

    }

    //==============================================================================================
    // methods for websockets
    fun initWebSocket(){
        Log.i(TAG, "init Web Socket")
        val (sslContext, trustManager) = getDevSSLContext(this)
        client = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager).build()
        val listener = MyWebSocket()

        val request = Request.Builder().url(Constants().SERVER_WEBSOCKET_URL).addHeader("userID", Constants().USER_ID.toString()).build()  // addHeader("userID", Constants().USER_ID.toString()).

        val wss = client.newWebSocket(request, listener)
        Log.i(TAG, wss.toString())
    }

    private fun getDevSSLContext(context: Context): Pair<SSLContext, TrustManager> {
        // Load developer certificate
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream =
            BufferedInputStream(context.resources.openRawResource(R.raw.dev_der_crt))
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        // Create an SSLContext that uses our TrustManager
        return Pair(SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }, tmf.trustManagers[0])
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
