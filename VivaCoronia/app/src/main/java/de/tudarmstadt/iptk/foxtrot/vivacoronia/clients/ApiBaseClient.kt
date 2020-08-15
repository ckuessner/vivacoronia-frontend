package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.BuildConfig
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import org.json.JSONArray
import org.json.JSONObject
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.LoginActivity
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.*


abstract class ApiBaseClient {
    private val _tag = "ApiBaseClient"

    fun getBaseUrl(): String {
        return Constants.SERVER_BASE_URL
    }


    fun joinPaths(basePath: String, vararg paths: String): String {
        val builder = Uri.parse(basePath).buildUpon()
        paths.forEach { builder.appendPath(it) }
        return builder.build().toString()
    }

    fun getUserId(ctx: Context): String {
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        return settings.getString(Constants.USER_ID, null) as String
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
    companion object {
        fun getJWTHeaderS(jwt: String): MutableMap<String, String> {
            val params = HashMap<String, String>()
            params["jwt"] = jwt
            return params
        }
    }
    /*
    This method sends a volley request with JWTs.
     */
    protected class StringRequestJWT : StringRequest{
        private var jsonObj : JSONObject? = null
        private var jsonArr : JSONArray? = null
        private val jwt : String
        constructor(method: Int,
                    url: String,
                    req: Response.Listener<String>,
                    error: Response.ErrorListener? = null, ctx: Context, jsonArr : JSONArray? = null, jsonObj : JSONObject? = null) : super(method, url, req, ErrorJWTCheck(ctx, error)) {
            this.jsonArr = jsonArr
            this.jsonObj = jsonObj
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.JWT, null) as String
        }

        inner class JWTRequestException : java.lang.Exception("You didn't input a jsonObject or a jsonArray")

        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt)
        }
        override fun getBodyContentType(): String = "application/json; charset=utf-8"
        override fun getBody(): ByteArray {
            return when {
                jsonArr != null -> jsonArr.toString().toByteArray(Charsets.UTF_8)
                jsonObj != null -> jsonObj.toString().toByteArray(Charsets.UTF_8)
                else -> throw JWTRequestException()
            }
        }
    }


    protected class JsonObjectJWT : JsonObjectRequest {
        private val jwt : String
        private val ctx: Context
        constructor(method: Int, url : String, body : JSONObject? = null, list : Listener<JSONObject>, error: Response.ErrorListener? = null,  ctx : Context) : super(method, url, body, list, ErrorJWTCheck(ctx, error)){
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString("jwt", null) as String
            this.ctx = ctx
        }
        constructor(url: String, body: JSONObject? = null, list : Listener<JSONObject>, error: Response.ErrorListener? = null, ctx: Context) : super(url, body, list, ErrorJWTCheck(ctx, error)){
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString("jwt", null) as String
            this.ctx = ctx
        }
        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt)
        }
    }


    protected class JsonArrayJWT : JsonArrayRequest {
        private val ctx : Context
        private val jwt : String
        constructor(url : String, list: Listener<JSONArray>, error: Response.ErrorListener? = null, ctx: Context) : super(url, list, ErrorJWTCheck(ctx, error)){
            this.ctx = ctx
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString("jwt", null) as String
        }
        constructor(method: Int, url: String, body: JSONArray? = null, list: Listener<JSONArray>, error: Response.ErrorListener? = null, ctx : Context) : super(method, url, body, list, ErrorJWTCheck(ctx, error)){
            this.ctx = ctx
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString("jwt", null) as String
        }
        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt)
        }
    }


    /*
    this class catches 401 authentication error and tries to create a new jwt for access by logging in

     */
    private class ErrorJWTCheck(val ctx : Context, val errorSuper : Response.ErrorListener?) : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            if (error?.networkResponse != null) {
                if(error.networkResponse.statusCode == 401){
                    val intent = Intent(ctx, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                    errorSuper?.onErrorResponse(error)
                }
            }
        }
    }
}
