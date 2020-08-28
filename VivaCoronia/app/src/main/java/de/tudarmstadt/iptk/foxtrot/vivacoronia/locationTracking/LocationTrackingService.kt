package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import de.tudarmstadt.iptk.foxtrot.vivacoronia.NotificationHelper
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LocationTrackingService : Service() {
    // used this link as a hint
    // https://bitbucket.org/tiitha/backgroundserviceexample/src/master/app/src/main/java/com/geoape/backgroundlocationexample/BackgroundService.java
    private val tag = "LocationTrackingService"

    lateinit var context: Context
    private lateinit var locManager: LocationManager
    private lateinit var locListener: LocationListener
    private lateinit var notification: Notification

    // TODO type should be changed according to the Rest api
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "startet location tracking service")
        notification = NotificationHelper.getNotification(
            this,
            Constants.LOCATION_NOTIFICATION_CHANNEL_ID,
            R.drawable.ic_corona,
            getString(R.string.location_service_channel_title),
            "",
            NotificationCompat.PRIORITY_DEFAULT,
            Color.RED
        )
        // has to be called at least 5 sec after services starts
        startForeground(Constants.LOCATION_NOTIFICATION_ID, notification)

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
        //Toast.makeText(context, getString(R.string.location_service_toast_started), Toast.LENGTH_SHORT).show()

        try {
            // location get requested with an delay of min 30sec and if the locations differ 15m
            locManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Constants.LOCATION_TRACKING_MIN_UPDATE_TIME,
                Constants.LOCATION_TRACKING_MIN_UPDATE_DISTANCE,
                locListener
            )
        }
        catch (se: SecurityException){
            Log.e(tag, "SecurityException", se)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(tag, "Location Tracking Service destroyed :(")
    }

    suspend fun addLocationToDatabase(location: Location){
        coroutineScope {
            val sdf = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.GERMANY) // "yyyy-MM-dd'T'HH:mm:ss.SSS"
            val date = sdf.format(Date())
            Log.i(tag, date)
            db.coronaDao().addLocation(DBLocation(date, location.longitude, location.latitude))
            Log.i(tag, "new Location added at <" + date + ">: " + location.longitude.toString() + ", " + location.latitude.toString())
        }
    }

    inner class MyLocationListener: LocationListener {
        override fun onLocationChanged(p0: Location?) {
            if (p0 != null) {
                // database write has to be asynchronous because this service runs in main thread
                GlobalScope.launch {
                    // the time of the location object does not necessarily have to equal to the system time, so we have to get the System time seperatly and change the location time to the system time
                    addLocationToDatabase(p0)
                }
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

        override fun onProviderEnabled(p0: String?) {}

        override fun onProviderDisabled(p0: String?) {
            // TODO evt user informieren
        }

    }
}
