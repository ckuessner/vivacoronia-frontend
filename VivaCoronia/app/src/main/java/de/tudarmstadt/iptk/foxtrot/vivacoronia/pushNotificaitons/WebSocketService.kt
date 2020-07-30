package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.X509TrustManager

class WebSocketService : Service() {
    private val TAG = "WebSocketService"

    private lateinit var client : OkHttpClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "started upload Service")

        initWebSocket()

        return super.onStartCommand(intent, flags, startId)
    }

    //==============================================================================================
    // methods for web sockets
    private fun initWebSocket(){
        Log.i(TAG, "init Web Socket")
        val (sslContext, trustManager) = getDevSSLContext(this)
        client = OkHttpClient.Builder().sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager).build()
        val listener = PushNotificationListener()
        listener.socketService = this

        val request = Request.Builder().url(Constants.SERVER_WEBSOCKET_URL).addHeader("userID", Constants.USER_ID.toString()).build()  // addHeader("userID", Constants.USER_ID.toString()).

        val wss = client.newWebSocket(request, listener)
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
