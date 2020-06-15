package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

abstract class ApiBaseClient {
    private val _tag = "ApiBaseClient"

    fun getBaseUrl(): String {
        return Constants().SERVER_BASE_URL
    }

    fun getUserId(): Int {
        return Constants().USER_ID
    }

    fun getRequestQueue(context: Context): RequestQueue {
        return try {
            val sslContext =
                getDevSSLContext(
                    context
                )
            // To use the dev certificate, provide HurlStack with SSLSocketFactory from custom context
            Volley.newRequestQueue(
                context,
                HurlStack(null, sslContext.socketFactory)
            )
        } catch (e: Exception) {
            Log.e(_tag, "could not load dev cert: $e")
            Volley.newRequestQueue(context)
        }
    }

    /**
     * Loads the dev certificate and creates a custom SSLContext
     *
     * See: https://developer.android.com/training/articles/security-ssl.html#CommonProblems
     */
    private fun getDevSSLContext(context: Context): SSLContext {
        // Load developer certificate
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream = BufferedInputStream(context.resources.openRawResource(R.raw.dev_der_crt))
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
        return SSLContext.getInstance("TLS").apply {
            init(null, tmf.trustManagers, null)
        }
    }
}