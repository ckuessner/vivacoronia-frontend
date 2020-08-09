package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.BuildConfig
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext

abstract class ApiBaseClient {
    private val _tag = "ApiBaseClient"

    fun getBaseUrl(): String {
        return Constants.SERVER_BASE_URL
    }

    fun getUserId(): Int {
        return Constants.USER_ID
    }

    fun joinPaths(basePath: String, vararg paths: String): String {
        val builder = Uri.parse(basePath).buildUpon()
        paths.forEach { builder.appendPath(it) }
        return builder.build().toString()
    }

    private fun checkInternetPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequestQueue(context: Context): RequestQueue? {
        if (!checkInternetPermissions(context)) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_no_internet_permissions),
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        if (BuildConfig.DEBUG) {
            return try {
                val sslContext =
                    getDevSSLContext(
                        context
                    ).first
                // To use the dev certificate, provide HurlStack with SSLSocketFactory from custom context
                Volley.newRequestQueue(
                    context,
                    HurlStack(null, sslContext.socketFactory)
                )
            } catch (e: Exception) {
                Log.e(_tag, "could not load dev cert: $e")
                Volley.newRequestQueue(context)
            }
        } else {
            return Volley.newRequestQueue(context)
        }
    }
}