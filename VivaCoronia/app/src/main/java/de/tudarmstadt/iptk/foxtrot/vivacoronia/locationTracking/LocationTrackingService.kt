package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Notification
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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.Entities.DBLocation
import kotlinx.coroutines.*

class LocationTrackingService : Service() {
    // use this link as a hint
    // https://bitbucket.org/tiitha/backgroundserviceexample/src/master/app/src/main/java/com/geoape/backgroundlocationexample/BackgroundService.java
    private val TAG = "LocationTrackingService"

    lateinit var context: Context
    lateinit var locManager: LocationManager
    lateinit var locListener: LocationListener
    lateinit var notification: Notification

    // TODO type should be changed according to the Rest api
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        notification = LocationNotificationHelper.getLocationNotification(this)
        // has to be called at least 5 sec after services starts
        startForeground(LOCATION_NOTIFICATION_ID, notification)

        //
        context = this
        locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locListener = MyLocationListener()

        //
        db = AppDatabase.getDatabase(context)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // inform user that tracking is now active
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
        Log.e(TAG, "Locatoin Tracking Service destroyed :(")
        super.onDestroy()
    }

    suspend fun addLocationToDatabase(location: Location){
        coroutineScope {
            db.coronaDao().addLocation(DBLocation(location.time, location.longitude, location.latitude))
        }
    }

    inner class MyLocationListener: LocationListener {

        private var TAG = "MyLocationListener"

        override fun onLocationChanged(p0: Location?) {
            if (p0 != null) {
                // database write has to be asyncronous because this service runs in main thread
                GlobalScope.launch {
                    addLocationToDatabase(p0)
                    Log.i(TAG, "write in database")}
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
            // TODO evt user informieren
        }

    }
}
