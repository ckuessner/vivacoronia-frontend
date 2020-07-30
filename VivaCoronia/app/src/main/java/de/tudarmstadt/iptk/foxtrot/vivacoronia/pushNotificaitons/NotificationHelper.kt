package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity

object NotificationHelper{

    /**
     * called when the main activity is started
     * @param context:
     * @param channelName: name of the channel
     * @param channelDescription: description of the channel
     * @param channelImportance: importance of the channel
     * @param channelID: unique ID for the channel
     */
    fun createNotificationChannel(context: Context,
                                channelName: String,
                                channelDescription: String,
                                channelImportance: Int,
                                channelID: String
                                ) {
        // if we use api lower 26 we have to check it here because this is only available for 26 and higher
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelID, channelName, channelImportance).apply {
                description = channelDescription
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * @param context:
     * @param channelID: the channel in which the notification shall be displayed
     * @param smallIcon: the icon of the notification
     * @param title: the notification title
     * @param text: the notification text
     * @param priority: a Constant from the NoficationCompat class like NotificationCompat.PRIORITY_HIGH
     * @param color: a Constant from the Colors class like Color.RED
     */
    fun getNotification(context: Context,
                        channelID: String,
                        smallIcon: Int,
                        title: String,
                        text: String,
                        priority: Int,
                        color: Int
                        ) : Notification {
        return NotificationCompat.Builder(context, channelID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority).setColor(color)
            .setContentIntent(Intent(context, MainActivity::class.java).let {
                    notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, 0)})
            .build()
    }

}