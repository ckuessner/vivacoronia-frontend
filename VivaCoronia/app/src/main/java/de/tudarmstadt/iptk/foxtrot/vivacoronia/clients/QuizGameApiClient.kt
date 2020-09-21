package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import com.android.volley.Request.Method.POST
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

object QuizGameApiClient : ApiBaseClient() {
    private const val TAG = "QuizGameClient"

    private fun getEndpoint(): String {
        return "${getBaseUrl()}/quiz/game/"
    }

    fun postQuizGameRequest(ctx: Context, location: LatLng) {
        val requestQueue = getRequestQueue(ctx) ?: throw VolleyError(REQUEST_QUEUE_ERR_STRING)
        val url = getEndpoint()
        val locationJsonObject = JSONObject().put(
            "location",
            JSONArray(arrayOf(location.longitude, location.latitude))
        )
        val response = RequestFuture.newFuture<JSONObject>()

        val request = JsonObjectJWT(POST, url, locationJsonObject, response, response, ctx)
        requestQueue.add(request)

        response.get()
    }

    fun getGameInfo(ctx: Context, gameId: String) {

    }

    fun postGameAnswer(ctx: Context, gameId: String, answer: String) {

    }

    private fun GameObject

}