package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.util.Log
import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.OpponentInfo
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.QuizGameDto
import org.json.JSONArray
import org.json.JSONObject

object QuizGameApiClient : ApiBaseClient() {
    private const val TAG = "QuizGameClient"

    private val klaxonWithQuestion: Klaxon = Klaxon()
    private val klaxon: Klaxon = Klaxon()

    private fun getEndpoint(): String {
        return "${getBaseUrl()}/quiz/game/"
    }


    fun postQuizGameRequest(ctx: Context, location: LatLng): Pair<QuizGameDto, OpponentInfo> {
        val requestQueue = getRequestQueue(ctx) ?: throw VolleyError(REQUEST_QUEUE_ERR_STRING)
        val url = getEndpoint()
        val locationJsonObject = JSONObject().put(
            "location",
            JSONArray(arrayOf(location.longitude, location.latitude))
        )
        val responseFuture = RequestFuture.newFuture<JSONObject>()
        val handler404Error = Response.ErrorListener { error ->
            error?.networkResponse?.statusCode == 404 && throw VolleyError("404 du Eumel")
            throw error
        }

        val request =
            JsonObjectJWT(POST, url, locationJsonObject, responseFuture, handler404Error, ctx)
        requestQueue.add(request)

        val gameJson = responseFuture.get()

        val game = klaxonWithQuestion.parse<QuizGameDto>(gameJson.get("game").toString())
        val oppInfo = klaxon.parse<OpponentInfo>(gameJson.get("opponentInfo").toString())

        if (game == null || oppInfo == null) throw VolleyError("Unable to create new game")

        return Pair(game, oppInfo)
    }

    fun getGameInfo(ctx: Context, gameId: String): QuizGameDto {
        val requestQueue = getRequestQueue(ctx) ?: throw VolleyError(REQUEST_QUEUE_ERR_STRING)
        val url = getEndpoint() + gameId
        val responseFuture = RequestFuture.newFuture<String>()
        val handler404Error = Response.ErrorListener { error ->
            error?.networkResponse?.statusCode == 404 && throw VolleyError("404 du Eumel")
            throw error
        }

        val request = StringRequestJWT(GET, url, responseFuture, handler404Error, ctx, null, false)

        requestQueue.add(request)

        val gameJson = responseFuture.get()
        return klaxonWithQuestion.parse(gameJson) ?: throw VolleyError("Could not get gameInfo")
    }

    fun getMultipleGames(ctx: Context, gameIds: List<String>): List<QuizGameDto> {
        val requestQueue = getRequestQueue(ctx) ?: throw VolleyError(REQUEST_QUEUE_ERR_STRING)

        return gameIds.map { gameId ->
            val url = getEndpoint() + gameId
            val responseFuture = RequestFuture.newFuture<String>()
            val request =
                StringRequestJWT(GET, url, responseFuture, responseFuture, ctx, null, false)
            requestQueue.add(request)
            responseFuture
        }.map {
            klaxonWithQuestion.parse<QuizGameDto>(it.get())
                ?: throw VolleyError("Could not get gameInfo")
        }.toList()

    }

    fun postGameAnswer(ctx: Context, gameId: String, questionIndex: Int, answer: String) {
        val requestQueue = getRequestQueue(ctx) ?: throw VolleyError(REQUEST_QUEUE_ERR_STRING)
        val url = getEndpoint() + gameId + "/answers/"
        val responseFuture = RequestFuture.newFuture<String>()
        val bodyJson = JsonObject(
            mapOf(
                "userId" to getUserId(ctx),
                "questionIndex" to questionIndex.toString(),
                "answer" to answer
            )
        ).toString()
        val request =
            StringRequestJWT(POST, url, responseFuture, responseFuture, ctx, bodyJson, false)

        requestQueue.add(request)
    }
}
