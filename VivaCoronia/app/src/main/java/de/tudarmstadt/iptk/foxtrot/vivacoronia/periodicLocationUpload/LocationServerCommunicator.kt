package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

object LocationServerCommunicator {
    private const val TAG = "LocationSending"

    /**
     * Sends the tracked locations of the user to the server
     *
     * @return true, if upload successful
     */
    fun sendPositionsToServer(context: Context, userID: Int, locations: List<DBLocation>) {
        if (!checkInternetPermissions(context)) {
            // Permission is not granted
            Toast.makeText(
                context,
                context.getString(R.string.location_upload_service_toast_no_internet),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Create JSONArray for request body with location records in it
        val locationJSONArray = marshalLocationRecordArray(locations)

        // build a single request
        val baseUrl = Constants().SERVER_BASE_URL
        val url = "$baseUrl/locations/$userID"

        val jsonStringRequest = JSONArrayRequest(
            Request.Method.POST, url, locationJSONArray,
            Response.Listener { response ->
                Log.i(TAG, "server response: $response")
                GlobalScope.launch {
                    // since upload was successful, delete the entries from the db
                    val db = AppDatabase.getDatabase(context)
                    db.coronaDao().deleteLocations(locations)
                }
            },
            Response.ErrorListener { error ->
                error.printStackTrace()
                Log.e(TAG, "upload failed: $error")
            }
        )

        // Add the upload request to the request queue
        Log.i(TAG, "uploading to $url: $locationJSONArray")
        getRequestQueue(context)
            .add(jsonStringRequest)
    }

    /**
     * Marshal the location record list to a JSONArray
     */
    private fun marshalLocationRecordArray(data: List<DBLocation>): JSONArray {
        return JSONArray(data.map { dataEntry ->
            JSONObject()
                .put("time", dataEntry.time)
                .put(
                    "location",
                    JSONObject()
                        .put("type", "Point")
                        .put(
                            "coordinates",
                            JSONArray()
                                .put(dataEntry.longitude)
                                .put(dataEntry.latitude)
                        )
                )
        })
    }

    private fun checkInternetPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    private class JSONArrayRequest(
        method: Int,
        url: String,
        val jsonArray: JSONArray,
        req: Response.Listener<String>,
        error: Response.ErrorListener
    ) : StringRequest(method, url, req, error) {
        override fun getBodyContentType(): String = "application/json; charset=utf-8"
        override fun getBody(): ByteArray = jsonArray.toString().toByteArray(Charsets.UTF_8)
    }

    private fun getRequestQueue(context: Context): RequestQueue {
        return try {
            val sslContext = getDevSSLContext(context)
            // To use the dev certificate, provide HurlStack with SSLSocketFactory from custom context
            Volley.newRequestQueue(
                context,
                HurlStack(null, sslContext.socketFactory)
            )
        } catch (e: Exception) {
            Log.e(TAG, "could not load dev cert: $e")
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