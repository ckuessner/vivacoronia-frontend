package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import de.tudarmstadt.iptk.foxtrot.vivacoronia.LOCATION_NOTIFICATION_CHANNEL_ID
import de.tudarmstadt.iptk.foxtrot.vivacoronia.LOCATION_NOTIFICATION_ID
import de.tudarmstadt.iptk.foxtrot.vivacoronia.MainActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R


class LocationNotificationHelper{
    companion object{
        /**
         * partially from https://developer.android.com/training/notify-user/build-notification
         */
        fun createLocationNotificationChannel(context: Context) {
            // if we use api lower 26 we have to check it here because this is only available for 26 and higher
            if (Build.VERSION.SDK_INT >= 26) {
                val name = "LocationTrackingChannel"
                val descriptionText = "Shows the user that tracking is active"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(LOCATION_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun getLocationNotification(context: Context) : Notification {
            return NotificationCompat.Builder(context, LOCATION_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_corona)     // without icon a default notification would be displayed
                .setContentTitle("Corona Tracking aktiv")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setColor(Color.RED)
                .setContentIntent(Intent(context, MainActivity::class.java).let {notificationIntent -> PendingIntent.getActivity(context, 0, notificationIntent, 0)})
                .build()
        }
    }
}

