package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
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
                val name = context.getString(R.string.location_service_channel_name)
                val descriptionText = context.getString(R.string.location_service_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(Constants().LOCATION_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun getLocationNotification(context: Context) : Notification {
            return NotificationCompat.Builder(context, Constants().LOCATION_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_corona)     // without icon a default notification would be displayed
                .setContentTitle(context.getString(R.string.location_service_channel_title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setColor(Color.RED)
                .setContentIntent(Intent(context, MainActivity::class.java).let {notificationIntent -> PendingIntent.getActivity(context, 0, notificationIntent, 0)})
                .build()
        }
    }
}

