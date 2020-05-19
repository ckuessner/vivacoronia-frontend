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

class LocationTrackingService : Service() {

    lateinit var context: Context
    lateinit var locManager: LocationManager
    lateinit var locListener: LocationListener

    var iBinder: IBinder? = null

    lateinit var locationBuffer: MutableList<Location>     // TODO type should be changed according to the Rest api

    override fun onCreate() {
        super.onCreate()
        context = this
        locManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locListener = MyLocationListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // startForeground()

        // inform user that tracking is now done
        Toast.makeText(context, "Location Tracking active ...", Toast.LENGTH_SHORT)

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 15 as Float, locListener)     // location get requested with an delay of min 30sec and if the locatoins differ 15m

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
                Log.i(TAG, "new Location added at <" + p0.time.toString() + ">: " + p0.longitude.toString() + " - " + p0.latitude.toString())
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(p0: String?) {
            TODO("request from user to enable provider")
        }

    }
}
