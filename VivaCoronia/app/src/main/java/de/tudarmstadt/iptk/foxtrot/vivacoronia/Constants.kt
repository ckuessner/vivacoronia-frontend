package de.tudarmstadt.iptk.foxtrot.vivacoronia

object Constants {

    data class DataPoint(val x: Double, val y: Double, val time: String)
    // Location Upload stuff
    val LOCATION_UPLOAD_INTERVAL = 10000.toLong() // for reallife use could be higher
    val LOCATION_UPLOAD_REQUEST_CODE = 12

    // Location stuff
    val LOCATION_TRACKING_REQUEST_INTERVAL = 30000.toLong()     // every 30sec

    val LOCATION_TRACKING_MIN_UPDATE_TIME = 0.toLong()      // 0secs
    val LOCATION_TRACKING_MIN_UPDATE_DISTANCE = 15.toFloat()    // 15m

    val LOCATION_NOTIFICATION_ID = 42   // must not be 0
    val LOCATION_NOTIFICATION_CHANNEL_ID = "location_channel"

    val LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 56    // requests the permission to use location data from the user
    val LOCATION_ACCESS_SETTINGS_REQUEST_CODE = 57      // requests access to gps, wifi, cellular network to access location data from the user
    val COARSE_LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 58

    val CAMERA_PERMISSION_REQUEST_CODE = 59

    val USER_ID = "userID"
    val CLIENT = "client_settings"
    val JWT = "jwt"
    val adminJWT = "adminJWT"
    // infected stuff
    val INFECTED_NOTIFICATION_CHANNEL_ID = "infected_channel"

    val SERVER_WEBSOCKET_URL = BuildConfig.WEBSOCKET_SERVER
    val SERVER_BASE_URL = BuildConfig.API_SERVER


    val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"
}
