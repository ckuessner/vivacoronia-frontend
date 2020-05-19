package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.*

class LocationTrackingService : Service() {

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

        locationBuffer = ArrayList<Location>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var builder = LocationNotificationHelper.getLocationNotificationBuilder(this)
        val notification = builder.build()
        startForeground(LOCATION_NOTIFICATION_ID, notification)

        // inform user that tracking is now done
        Toast.makeText(context, "Location Tracking active ...", Toast.LENGTH_SHORT)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        // location get requested with an delay of min 30sec and if the locatoins differ 15m
        locManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_TRACKING_MIN_UPDATE_TIME,
            LOCATION_TRACKING_MIN_UPDATE_DISTANCE,
            locListener)



        // start the service againg if it was killen
        return Service.START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        // TODO save the received locations by making api request
        // start service which sends locations to the server over the rest api
        return super.stopService(name)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
        return iBinder!!
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
            Log.e(TAG, "onStatusChanged: " + p0 + ", " + p1)
        }

        override fun onProviderEnabled(p0: String?) {
            Log.i(TAG, "onProviderEnabled:" + p0)
        }

        override fun onProviderDisabled(p0: String?) {
            Log.e(TAG, "onProviderDisabled: " + p0)
        }

    }
}
