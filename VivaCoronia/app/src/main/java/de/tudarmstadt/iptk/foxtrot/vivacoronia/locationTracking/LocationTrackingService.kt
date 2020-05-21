package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.*
import kotlin.collections.ArrayList

class LocationTrackingService : Service() {
    // use this link as a hint
    // https://bitbucket.org/tiitha/backgroundserviceexample/src/master/app/src/main/java/com/geoape/backgroundlocationexample/BackgroundService.java
    private val TAG = "LocationTrackingService"

    lateinit var context: Context
    lateinit var locManager: LocationManager
    lateinit var locListener: LocationListener

    var iBinder: IBinder? = null

    lateinit var locationBuffer: ArrayList<Location>     // TODO type should be changed according to the Rest api

    override fun onCreate() {
        super.onCreate()
        context = this
        locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locListener = MyLocationListener()

        locationBuffer = ArrayList()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val builder = LocationNotificationHelper.getLocationNotificationBuilder(this)
        val notification = builder.build()
        // has to be called at least 5 sec after services starts
        startForeground(LOCATION_NOTIFICATION_ID, notification)

        // inform user that tracking is now done
        Toast.makeText(context, "Location Tracking active ...", Toast.LENGTH_SHORT).show()

        try {
            // location get requested with an delay of min 30sec and if the locatoins differ 15m
            locManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_TRACKING_MIN_UPDATE_TIME,
                LOCATION_TRACKING_MIN_UPDATE_DISTANCE,
                locListener
            )
        }
        catch (se: SecurityException){
            Log.e(TAG, "SecurityException")
        }

        // start the service againg if it was killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO write the location buffer into the server
    }

    private fun uploadLocations() {
        // TODO upload locations
    }


    inner class MyLocationListener: LocationListener {

        private var TAG = "MyLocationListener"

        override fun onLocationChanged(p0: Location?) {
            if (p0 != null) {
                locationBuffer.add(p0)
                Log.i(TAG, "new Location added at <" + p0.time.toString() + ">: " + p0.longitude.toString() + ", " + p0.latitude.toString())
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            Log.i(TAG, "onStatusChanged: " + p0 + ", " + p1)
        }

        override fun onProviderEnabled(p0: String?) {
            Log.i(TAG, "onProviderEnabled:" + p0)
        }

        override fun onProviderDisabled(p0: String?) {
            Log.i(TAG, "onProviderDisabled: " + p0)
            // TODO open popup to inform user
        }

    }
}
