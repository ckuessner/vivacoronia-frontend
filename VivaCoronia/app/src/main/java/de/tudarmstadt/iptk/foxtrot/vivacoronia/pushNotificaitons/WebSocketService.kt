package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.NotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.net.ssl.X509TrustManager

class WebSocketService : Service() {
    private val TAG = "WebSocketService"

    private lateinit var client : OkHttpClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "started upload Service")

        initWebSocket()

        return START_STICKY
    }

    //==============================================================================================
    // methods for web sockets
    // for okhttp see license
    fun initWebSocket(){
        Log.i(TAG, "init Web Socket")
        val (sslContext, trustManager) = getDevSSLContext(this)
        client = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager).build()
        val listener = PushNotificationListener()
        listener.socketService = this

        val request = Request.Builder().url(Constants.SERVER_WEBSOCKET_URL).addHeader("userID", Constants.USER_ID.toString()).build()

        val wss = client.newWebSocket(request, listener)
        wss.send("Ping")
        Log.i(TAG, wss.toString())
    }


    // make notification
    fun makeNotification(){
        with(NotificationManagerCompat.from(this)){
            notify(
                SystemClock.elapsedRealtime().hashCode(), //we want a unique id so that notifications for different contacts overwrite each other
                NotificationHelper.getNotification(
                    applicationContext,
                    Constants.INFECTED_NOTIFICATION_CHANNEL_ID,
                    R.drawable.ic_corona,
                    getString(R.string.infected_notification_channel_title),
                    getString(R.string.infected_notification_channel_text),
                    NotificationCompat.PRIORITY_HIGH,
                    Color.RED
                )
            )
        }
    }


    fun reconnect() {
    /*    // alarmmanager because this shall also be triggered if the app is not running but the
        // connection to the websocket is lost
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(this, WebSocketReconnectService::class.java)
        val pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (pendingIntent != null) {
            // try to reconnect in 10 seconds, but dont wakeup device if asleep
            Log.i("WebSocketService", "start pending websocket service start")
            alarmManager?.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+10000, pendingIntent)
        } */
        tryStartWebSocketService(this)

        stopSelf()
    }

    companion object{
        fun tryStartWebSocketService(context: Context){
            // alarmmanager because this shall also be triggered if the app is not running but the
            // connection to the websocket is lost
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, WebSocketReconnectService::class.java)
            val pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (pendingIntent != null) {
                // try to reconnect in 10 seconds, but dont wakeup device if asleep
                Log.i("WebSocketService", "start pending websocket service start")
                alarmManager?.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+10000, pendingIntent)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
