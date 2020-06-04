package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.app.AlarmManager

class Constants {

    data class DataPoint(val x: Double, val y: Double, val time: String)

    // Location Upload stuff
    val LOCATION_UPLOAD_INTERVAL = AlarmManager.INTERVAL_HALF_DAY   // upload data every half a day for Debugging use smaller interval like 10000.toLong()
    val LOCATION_UPLOAD_REQUEST_CODE = 12

    // Location stuff
    val LOCATION_TRACKING_REQUEST_INTERVAL = 30000.toLong()     // every 30sec

    val LOCATION_TRACKING_MIN_UPDATE_TIME = 0.toLong()      // 30secs
    val LOCATION_TRACKING_MIN_UPDATE_DISTANCE = 15.toFloat()    // 15m

    val LOCATION_NOTIFICATION_ID = 42   // must not be 0
    val LOCATION_NOTIFICATION_CHANNEL_ID = "location_channel"

    val LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 56    // requests the permission to use location data from the user
    val LOCATION_ACCESS_SETTINGS_REQUEST_CODE = 57      // requests access to gps, wifi, cellular network to access location data from the user

    val USER_ID = 1234

    // local ip if you use an emulator, localhost if you use an external android device with port forwarding
    // otherwise this constant will contain the base url to the server api
    val SERVER_BASE_URL = "http://localhost:3000"

    val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"
}

