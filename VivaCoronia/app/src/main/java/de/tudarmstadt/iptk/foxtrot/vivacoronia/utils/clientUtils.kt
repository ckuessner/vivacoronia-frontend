package de.tudarmstadt.iptk.foxtrot.vivacoronia.utils

import android.content.Context
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory


/**
 * Loads the dev certificate and creates a custom SSLContext
 *
 * See: https://developer.android.com/training/articles/security-ssl.html#CommonProblems
 */
fun getDevSSLContext(context: Context): Pair<SSLContext, TrustManager> {
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


