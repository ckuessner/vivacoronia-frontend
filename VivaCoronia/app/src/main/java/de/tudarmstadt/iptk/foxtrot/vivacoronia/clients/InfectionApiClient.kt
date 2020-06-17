package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject

object InfectionApiClient : ApiBaseClient() {
    private fun getEndpoint(): String{
        return "${getBaseUrl()}/infection/${getUserId()}"
    }

    fun postInfectionStatus(context: Context, infectionStatusData: Map<String, String>, onUploadSuccessful: () -> Unit, onUploadFailed: (error: VolleyError) -> Unit){
        val queue = getRequestQueue(context) ?: return
        val url = getEndpoint()

        val requestBody = JSONObject(infectionStatusData).toString()
        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { onUploadSuccessful() },
            Response.ErrorListener { e -> onUploadFailed(e) }
        ) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        queue.add(request)
    }

    fun getInfectionStatus(context: Context): HashMap<String, String>{
        val queue = getRequestQueue(context) ?: return HashMap()
        val url = getEndpoint()
        val future = RequestFuture.newFuture<String>()
        val request = StringRequest(Request.Method.GET, url, future, future)

        queue.add(request)
        val mapper = ObjectMapper()
        val result = future.get()
        return if (result != "") mapper.readValue(result) else HashMap()
    }
}