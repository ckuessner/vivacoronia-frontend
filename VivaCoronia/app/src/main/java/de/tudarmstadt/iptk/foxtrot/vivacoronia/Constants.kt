package de.tudarmstadt.iptk.foxtrot.vivacoronia

class Constants {

    data class DataPoint(val x: Double, val y: Double, val time: Long)
    val LOCATION_TRACKING_REQUEST_INTERVAL = 30000.toLong()     // every 30sec
    val LOCATION_TRACKING_MIN_UPDATE_TIME = 30000.toLong()      // 30secs
    val LOCATION_TRACKING_MIN_UPDATE_DISTANCE = 15.toFloat()    // 15m
    val LOCATION_NOTIFICATION_ID = 42   // must not be 0
    val LOCATION_NOTIFICATION_CHANNEL_ID = "location_channel"
    val LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 56    // requests the permission to use location data from the user
    val LOCATION_ACCESS_SETTINGS_REQUEST_CODE = 57      // requests access to gps, wifi, cellular network to access location data from the user

    val USER_ID = 1234
    val SWAGGER_URL = "http://192.168.2.105:3000"
}