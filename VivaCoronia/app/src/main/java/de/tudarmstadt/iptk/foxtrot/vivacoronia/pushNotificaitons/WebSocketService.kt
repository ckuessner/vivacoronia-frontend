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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.util.concurrent.TimeUnit
import org.json.JSONObject
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
                .pingInterval(30, TimeUnit.SECONDS)
                .build()
        } else {
            client = OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build()
        }
        listener = PushNotificationListener()
        listener!!.socketService = this
        val userID = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
        val jwt = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.JWT, null) as String
        val request = Request.Builder().url(Constants.SERVER_WEBSOCKET_URL).addHeader("userID", userID).addHeader("jwt", jwt).build()

        client.newWebSocket(request, listener!!)
    }


    // make notification
    fun makeContactNotification(){
        with(NotificationManagerCompat.from(this)){
            notify(
                (SystemClock.elapsedRealtime()+1).hashCode(), //we want a unique id so that notifications for different contacts dont overwrite each other
                NotificationHelper.getSimpleNotification(
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
                (SystemClock.elapsedRealtime()+2).hashCode(), //we want a unique id so that notifications for different contacts overwrite each other
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
    
    fun makeAchievementNotification(msg: String) {
        with(NotificationManagerCompat.from(this)) {
            notify(
                (SystemClock.elapsedRealtime()+3).hashCode(), //we want a unique id so that notifications for different contacts overwrite each other
                NotificationHelper.getAchievementNotification(
                    applicationContext,
                    Constants.ACHIEVEMENT_NOTIFICATION_CHANNEL_ID,
                    R.drawable.ic_corona,
                    getString(R.string.achievement_notification_channel_title),
                    msg,
                    NotificationCompat.PRIORITY_DEFAULT,
                    Color.GREEN
                )
            )
        }
    }

    fun makeQuizNotification(type: String, content: JSONObject) {
        val text = when (type) {
            "QUIZ_NEW" -> "An opponent wants to play a quiz against you!"
            "QUIZ_TURN" -> "It's your turn in a quiz game!"
            "QUIZ_GAMEOVER_WON" -> "Congrats! You won a quiz game!"
            "QUIZ_GAMEOVER_LOST" -> "You lost a quiz game."
            "QUIZ_GAMEOVER_DRAW" -> "A quiz game resulted in a draw."
            else -> "Do you want to play a quiz?"
        }
        if (type == "QUIZ_NEW")
            AppDatabase.getDatabase(applicationContext).quizGameDao().insert(QuizGame(content["gameId"] as String, -1))
        val gameFinished = (type == "QUIZ_GAMEOVER_WON" || type == "QUIZ_GAMEOVER_LOST" || type == "QUIZ_GAMEOVER_DRAW")
        with(NotificationManagerCompat.from(this)) {
            notify(
                content["gameId"].hashCode(), // one notification per game
                NotificationHelper.getQuizNotification(
                    applicationContext,
                    Constants.QUIZ_NOTIFICATION_CHANNEL_ID,
                    R.drawable.ic_corona,
                    getString(R.string.quiz_notification_channel_title),
                    text,
                    NotificationCompat.PRIORITY_DEFAULT,
                    Color.RED,
                    content,
                    gameFinished
                )
            )
        }
    }

    fun reconnect() {
        Log.i(tag, "reconnect")
        tryStartWebSocketService(this)
    }

    companion object{
        private var listener: PushNotificationListener? = null

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

        fun testConnection(){
            if (listener != null && listener!!.webSocket != null){
                Log.i("WebSocketService", "send ping")
                listener!!.webSocket!!.send("Ping")
                return
            }
            throw ConnectException()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
