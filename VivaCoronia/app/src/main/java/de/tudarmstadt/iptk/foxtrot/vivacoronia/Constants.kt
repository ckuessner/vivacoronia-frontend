package de.tudarmstadt.iptk.foxtrot.vivacoronia

object Constants {


    // Location Upload stuff
    const val LOCATION_UPLOAD_INTERVAL = 10000.toLong() // for reallife use could be higher
    const val LOCATION_UPLOAD_REQUEST_CODE = 12

    // Location stuff
    const val LOCATION_TRACKING_REQUEST_INTERVAL = 30000.toLong()     // every 30sec

    const val LOCATION_TRACKING_MIN_UPDATE_TIME = 0.toLong()      // 0secs
    const val LOCATION_TRACKING_MIN_UPDATE_DISTANCE = 15.toFloat()    // 15m

    const val LOCATION_NOTIFICATION_ID = 42   // must not be 0
    const val LOCATION_NOTIFICATION_CHANNEL_ID = "location_channel"

    const val LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 56    // requests the permission to use location data from the user
    const val LOCATION_ACCESS_SETTINGS_REQUEST_CODE = 57      // requests access to gps, wifi, cellular network to access location data from the user

    const val CAMERA_PERMISSION_REQUEST_CODE = 59

    const val USER_ID = "userID"
    const val CLIENT = "client_settings"
    const val JWT = "jwt"
    const val adminJWT = "adminJWT"
    const val adminJWT_Time = "adminTime"
    const val JWT_Time = "jwtTime"
    const val IS_ADMIN = "adminStatus"
    const val ENDPOINT_ACHIEVEMENT = "achievements"
    const val ENDPOINT_SCORE = "infectionScore"

    // infected stuff
    const val INFECTED_NOTIFICATION_CHANNEL_ID = "infected_channel"

    const val PRODUCT_NOTIFICATION_CHANNEL_ID = "product_channel"

    const val SERVER_WEBSOCKET_URL = BuildConfig.WEBSOCKET_SERVER
    const val SERVER_BASE_URL = BuildConfig.API_SERVER

    // for error handling
    const val FORBIDDEN = 403
    const val AUTH_ERROR = 401
    const val NULL_QUEUE = -1
    const val NO_INTERNET = -2
    const val VOLLEY_ERROR = -3
    const val SERVER_ERROR = -4
    const val FIREWALL_ERROR = -5

    const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"
}
