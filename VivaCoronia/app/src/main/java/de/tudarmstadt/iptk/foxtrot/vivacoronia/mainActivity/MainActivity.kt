package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.createBackgroundLocationRequest
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.requestLocationService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload.setupUploadAlarm
import de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons.WebSocketService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.ConnectException

private const val PRODUCT_QUERY = "product"

class MainActivity : AppCompatActivity() {
    private val tag = "MainActivity"
    // TODO check wheter google play services has the right version
    // TODO add licencing for location api

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

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
            NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.product_notification_channel_name),
                getString(R.string.product_notification_channel_description),
                NotificationManager.IMPORTANCE_DEFAULT,
                Constants.PRODUCT_NOTIFICATION_CHANNEL_ID
            )
            NotificationHelper.createNotificationChannel(
                this,
                getString(R.string.achievement_notification_channel_name),
                getString(R.string.achievement_notification_channel_description),
                NotificationManager.IMPORTANCE_DEFAULT,
                Constants.ACHIEVEMENT_NOTIFICATION_CHANNEL_ID
            )
        }

        //setup logout button logic
        setUpLogoutLogic()

        // setup navigation
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.nav_fragment)
        navController.setGraph(R.navigation.nav_graph)
        // the location history view, trading view and achievements view are all root views
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.locationHistoryFragment,
                R.id.tradingFragmentNav,
                R.id.achievementsFragment,
                R.id.infectionStatusFragment,
                R.id.spreadMapFragment,
                R.id.statusCheckFragment
            ),
            findViewById<DrawerLayout>(R.id.drawer_layout))

        // setup default toolbar with navcontroller
        // changes need to made here if a custom toolbar shall be used
        setupActionBarWithNavController(navController, appBarConfiguration)

        // setup nav view
        navView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)

        // add actions for drawer menu item here
        navView.setNavigationItemSelectedListener { item ->
            navigateToFragment(item.itemId, null)
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(navView)
            return@setNavigationItemSelectedListener true
        }

        GlobalScope.launch { fetchCategories() }

        // start tracking
        de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.checkPermissionsAndStartTracking(
            this,
            true
        )
    }

    private fun navigateToFragment(itemId: Int, query: ProductSearchQuery?) {
            when (itemId) {
                R.id.menu_item_location_history -> {
                    navController.navigate(R.id.locationHistoryFragment)
                }
                R.id.menu_item_infection_update -> {
                    navController.navigate(R.id.infectionStatusFragment)
                }
                R.id.menu_item_trading -> {
                    navController.navigate(R.id.tradingFragmentNav)
                }
                R.id.menu_item_achievements -> {
                    navController.navigate(R.id.achievementsFragment)
                }
                R.id.menu_item_spreadmap -> {
                    navController.navigate(R.id.spreadMapFragment)
                }
                R.id.menu_item_statusCheck -> {
                    navController.navigate(R.id.statusCheckFragment)
                }
                // only needed for notification navigation
                R.id.search_offers -> {
                    val b = Bundle()
                    b.putParcelable("product_query", query)
                    navController.navigate(R.id.tradingFragmentNav, b)
                }
            }
    }

    private fun setUpLogoutLogic(){
        val logoutBtn = findViewById<Button>(R.id.btn_sing_out)
        logoutBtn.setOnClickListener {
            //make alert dialog to make sure user actually wants to logout
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle(getString(R.string.sign_out_warn))
            builder.setMessage(getString(R.string.sign_out_conf))
            //if user wants to use admin, he has to login for admin features
            builder.setPositiveButton(android.R.string.yes){ _, _ ->
                getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).edit().clear().apply()
                //https://stackoverflow.com/questions/6134103/clear-applications-data-programmatically
                (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
            }
            //need to include this or else the cancel button won't be visible
            builder.setNegativeButton(android.R.string.no){_, _ -> }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    fun hideAdminFeatures() {
        navView.menu.findItem(R.id.menu_item_spreadmap).isVisible = false
    }

    private fun showAdminFeatures() {
        navView.menu.findItem(R.id.menu_item_spreadmap).isVisible = true
    }

    fun changeStatusIcon(isAdmin: Boolean){
        if(isAdmin)
            navView.menu.findItem(R.id.menu_item_statusCheck).icon = ContextCompat.getDrawable(this, R.drawable.ic_admin)
        else
            navView.menu.findItem(R.id.menu_item_statusCheck).icon = ContextCompat.getDrawable(this, R.drawable.ic_user)
    }

    private fun fetchCategories() {
        try {
            val categories = TradingApiClient.getAllCategories(this@MainActivity).toMutableList()
            runOnUiThread {
                BaseProduct.categories.value = categories
            }
        } catch (e: Exception) {
            Log.d(tag, "Failed to fetch categories: ", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // setup upload alarm
        setupUploadAlarm(applicationContext)

        // ping the server to see if my websocket is online or whether to create a new one
        try {
            Log.i(tag, "test websocket")
            WebSocketService.testConnection()
        }
        catch (e: ConnectException) {
            Log.i(tag, "websocket test failed")
            val websocketIntent = Intent(this, WebSocketService::class.java)
            startService(websocketIntent)
        }

        val startFragment = intent.getIntExtra("startFragment", R.id.menu_item_location_history)
        var product: ProductSearchQuery? = null
        val startFragment = intent.getIntExtra("startFragment", R.id.menu_item_location_history)
        if (startFragment == R.id.search_offers) {
            product = intent.getParcelableExtra<ProductSearchQuery>(PRODUCT_QUERY)
        }
        navigateToFragment(startFragment, product)

        val settings = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        val isAdmin = settings.getBoolean(Constants.IS_ADMIN, false)
        if (isAdmin) showAdminFeatures() else hideAdminFeatures()
        val hasAdminJWT = settings.getString(Constants.adminJWT, null) != null
        val isAdminActive = isAdmin && hasAdminJWT
        changeStatusIcon(isAdminActive)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
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
        Log.i(tag, "onRequestPermissionResult")
        if (permissions.isEmpty() && grantResults.isEmpty()) {
            return
        }
        when (requestCode) {
            // handle location permission requests
            Constants.LOCATION_ACCESS_PERMISSION_REQUEST_CODE -> {
                // request permissions again if location permission not granted
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    Log.v(tag, "Location Access Denied")

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
                    Log.v(tag, "start location service")
                    requestLocationService(this, createBackgroundLocationRequest(), true)
                }
            }
        }
    }
}

