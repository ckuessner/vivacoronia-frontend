package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.BuildConfig
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.getDevSSLContext
import org.json.JSONArray
import org.json.JSONObject
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.LoginActivity
import com.android.volley.Response.Listener
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
        fun getJWTHeaderS(jwt: String, isAdmin: Boolean = false): MutableMap<String, String> {
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val params = HashMap<String, String>()
            params[getterString] = jwt
            return params
        }
    }
    /*
    This method sends a volley request with JWTs.
     */
    protected class StringRequestJWT : StringRequest{
        private val jwt : String
        private val isAdmin: Boolean
        private val body : String?
        constructor(method: Int, url: String, list: Listener<String>, error: Response.ErrorListener? = null, ctx: Context, body: String? = null,isAdmin : Boolean = false) : super(method, url, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val defValue = if(isAdmin) "notNull" else null
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, defValue) as String
            this.body = body
        }
        constructor(url : String, list: Listener<String>,error: Response.ErrorListener? = null, ctx: Context, body: String? = null, isAdmin : Boolean = false): super(url, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, null) as String
            this.body = body
        }


        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt, isAdmin)
        }
        override fun getBody(): ByteArray {
            return body?.toByteArray() ?: super.getBody()
        }

        override fun getBodyContentType(): String {
            return "application/json"
        }
    }


    protected class JsonObjectJWT : JsonObjectRequest {
        private val jwt : String
        private val ctx: Context
        private val isAdmin: Boolean
        constructor(method: Int, url : String, body : JSONObject? = null, list : Listener<JSONObject>, error: Response.ErrorListener? = null,  ctx : Context, isAdmin : Boolean = false) : super(method, url, body, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val defValue = if(isAdmin) "notNull" else null
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, defValue) as String
            this.ctx = ctx
        }
        constructor(url: String, body: JSONObject? = null, list : Listener<JSONObject>, error: Response.ErrorListener? = null, ctx: Context, isAdmin : Boolean = false) : super(url, body, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val defValue = if(isAdmin) "notNull" else null
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, defValue) as String
            this.ctx = ctx
        }
        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt, isAdmin)
        }
    }


    protected class JsonArrayJWT : JsonArrayRequest {
        private val ctx : Context
        private val jwt : String
        private val isAdmin: Boolean
        constructor(url : String, list: Listener<JSONArray>, error: Response.ErrorListener? = null, ctx: Context, isAdmin : Boolean = false) : super(url, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            this.ctx = ctx
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val defValue = if(isAdmin) "notNull" else null
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, defValue) as String
        }
        constructor(method: Int, url: String, body: JSONArray? = null, list: Listener<JSONArray>, error: Response.ErrorListener? = null, ctx : Context, isAdmin : Boolean = false) : super(method, url, body, list, ErrorJWTCheck(ctx, error, isAdmin)){
            this.isAdmin = isAdmin
            this.ctx = ctx
            val getterString = if(isAdmin) Constants.adminJWT else Constants.JWT
            val defValue = if(isAdmin) "notNull" else null
            this.jwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(getterString, defValue) as String
        }
        override fun getHeaders(): MutableMap<String, String> {
            return getJWTHeaderS(jwt, isAdmin)
        }
    }


    /*
    this class catches 401 and 403 authentication error and handles it depending on whether the calling request is an admin request

     */
    private class ErrorJWTCheck(val ctx : Context, val errorSuper : Response.ErrorListener?, val isAdmin : Boolean) : Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            if (error?.networkResponse != null) {
                if(error.networkResponse.statusCode == 401 && !isAdmin){
                    val intent = Intent(ctx, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("isAdmin", false)
                    ctx.startActivity(intent)
                }
                else if (error.networkResponse.statusCode == 401 && isAdmin){
                    val intent = Intent(ctx, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("isAdmin", true)
                    ctx.startActivity(intent)
                }
                else if (error.networkResponse.statusCode == 403 && isAdmin){
                    //delete admin stuff, since this error only occurs if user doesn't have admin permissions anymore
                    ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).edit().putBoolean(Constants.IS_ADMIN, false).apply()
                    ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).edit().putString(Constants.adminJWT, null).apply()
                    Toast.makeText(ctx, ctx.getString(R.string.adminRevoked), Toast.LENGTH_LONG).show()
                    //navigate to status to show user that he isn't admin anymore
                    val act = ctx as FragmentActivity
                    val navController = act.findNavController(R.id.nav_fragment)
                    navController.setGraph(R.navigation.nav_graph)
                    navController.navigate(R.id.statusCheckFragment)
                }
            }
            errorSuper?.onErrorResponse(error) ?: if (error != null) throw error
        }
    }
}
