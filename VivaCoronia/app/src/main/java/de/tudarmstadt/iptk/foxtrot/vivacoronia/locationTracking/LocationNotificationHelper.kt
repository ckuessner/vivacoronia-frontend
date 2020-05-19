package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.LOCATION_NOTIFICATION_CHANNEL_ID


class LocationNotificationHelper{
    companion object{
        /**
         * partially from https://developer.android.com/training/notify-user/build-notification
         */
        fun createLocationNotificationChannel(context: Context) {
            // if we use api lower 26 we have to check it here because this is only available for 26 and higher
            val name = "LocationTrackingChannel"
            val descriptionText = "Shows the user that tracking is active"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(LOCATION_NOTIFICATION_CHANNEL_ID.toString(), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun getLocationNotificationBuilder(context: Context) : NotificationCompat.Builder {
            return NotificationCompat.Builder(context, LOCATION_NOTIFICATION_CHANNEL_ID.toString()).setContentTitle("Corona Tracking aktiv").setPriority(
                NotificationCompat.PRIORITY_DEFAULT)
        }
    }
}

