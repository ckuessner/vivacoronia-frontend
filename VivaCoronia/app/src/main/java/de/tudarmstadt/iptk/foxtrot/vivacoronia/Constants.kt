package de.tudarmstadt.iptk.foxtrot.vivacoronia

val LOCATION_TRACKING_MIN_UPDATE_TIME = 30000.toLong()      // 30secs
val LOCATION_TRACKING_MIN_UPDATE_DISTANCE = 15.toFloat()    // 15m

val LOCATION_NOTIFICATION_ID = 42   // the id for the notification which is shown, so that the user sees that the service is active
val LOCATION_NOTIFICATION_CHANNEL_ID = 43

val LOCATION_ACCESS_PERMISSION_REQUEST_CODE = 56    // requests the permission to use location data from the user
val LOCATION_ACCESS_SETTINGS_REQUEST_CODE = 57      // requests access to gps, wifi, cellular network to access location data from the user