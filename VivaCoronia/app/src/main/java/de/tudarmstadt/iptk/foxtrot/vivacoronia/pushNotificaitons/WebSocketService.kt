package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
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


    private fun getDevSSLContext(context: Context): Pair<SSLContext, TrustManager> {
        // Load developer certificate
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream =
            BufferedInputStream(context.resources.openRawResource(R.raw.dev_der_crt))
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        // Create an SSLContext that uses our TrustManager
        return Pair(SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }, tmf.trustManagers[0])
    }

    // make notification
    fun makeNotification(){
        with(NotificationManagerCompat.from(this)){
            notify(Constants.INFECTED_NOTIFICATION_ID,
                InfectedNotificationHelper.getInfectedNotification(applicationContext))
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
