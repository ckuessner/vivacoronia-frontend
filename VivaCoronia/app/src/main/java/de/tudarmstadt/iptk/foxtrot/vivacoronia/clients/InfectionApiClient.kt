package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject

object InfectionApiClient : ApiBaseClient() {
    private fun getEndpoint(ctx: Context): String{
        val userID = getUserId(ctx)
        return "${getBaseUrl()}/infection/$userID"
    }

    fun postInfectionStatus(context: Context, infectionStatusData: JSONObject, onUploadSuccessful: () -> Unit, onUploadFailed: (error: VolleyError) -> Unit){
        val queue = getRequestQueue(context) ?: return
        val url = getEndpoint(context)
        val request = StringRequestJWT(Request.Method.POST, url, Response.Listener { onUploadSuccessful()}, Response.ErrorListener { e -> onUploadFailed(e)}, context)

        queue.add(request)
    }

    fun getInfectionStatus(context: Context): JSONObject{
        val queue = getRequestQueue(context) ?: return JSONObject()
        val url = getEndpoint(context)
        val future = RequestFuture.newFuture<String>()
        val request = StringRequestJWT(Request.Method.GET, url, future, future, context)
        queue.add(request)
        val result = future.get()
        return JSONObject(result)
    }
}