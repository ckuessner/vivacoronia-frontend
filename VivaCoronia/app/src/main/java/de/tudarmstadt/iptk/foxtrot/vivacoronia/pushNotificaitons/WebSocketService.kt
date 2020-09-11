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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.BuildConfig
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.NotificationHelper
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.net.ssl.X509TrustManager

class WebSocketService : Service() {
    private val tag = "WebSocketService"

    private lateinit var client : OkHttpClient

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "started upload Service")

        initWebSocket()

        return START_STICKY
    }

    //==============================================================================================
    // methods for web sockets
    // for okhttp see license
    private fun initWebSocket(){
        Log.i(tag, "init Web Socket")
        if (BuildConfig.DEBUG) {
            val (sslContext, trustManager) = getDevSSLContext(this)
            client = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager)
                .build()
        } else {
            client = OkHttpClient()
        }
        val listener = PushNotificationListener()
        listener.socketService = this
        val userID = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
        val request = Request.Builder().url(Constants.SERVER_WEBSOCKET_URL).addHeader("userID", userID).build()

        client.newWebSocket(request, listener)
    }


    // make notification
    fun makeContactNotification(){
        with(NotificationManagerCompat.from(this)){
            notify(
                SystemClock.elapsedRealtime().hashCode(), //we want a unique id so that notifications for different contacts overwrite each other
                NotificationHelper.getNormalNotification(
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

    fun makeProductMatchNotification(product: ProductSearchQuery){
        with(NotificationManagerCompat.from(this)){
            notify(
                SystemClock.elapsedRealtime().hashCode(), //we want a unique id so that notifications for different contacts overwrite each other
                NotificationHelper.getProductMatchNotification(
                    applicationContext,
                    Constants.PRODUCT_NOTIFICATION_CHANNEL_ID,
                    R.drawable.ic_corona,
                    getString(R.string.product_notification_channel_title) + " " + product.productName,
                    getString(R.string.product_notification_channel_text),
                    NotificationCompat.PRIORITY_DEFAULT,
                    Color.TRANSPARENT,
                    product
                )
            )
        }
    }


    fun reconnect() {
        Log.i(tag, "reconnect")
        tryStartWebSocketService(this)
    }

    companion object{
        fun tryStartWebSocketService(context: Context){
            // alarmmanager because this shall also be triggered if the app is not running but the
            // connection to the websocket is lost
            Log.i("tryStart", context.toString())
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, WebSocketReconnectService::class.java)
            val pendingIntent = PendingIntent.getService(context, 71, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if (pendingIntent != null) {
                // try to reconnect in 10 seconds, but dont wakeup device if asleep
                Log.i("WebSocketService", "start pending websocket service start")
                alarmManager?.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+10000, pendingIntent)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
