package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.RequestFuture
import com.beust.klaxon.*
import org.json.JSONArray
import org.threeten.bp.ZonedDateTime

object ContactApiClient : ApiBaseClient() {
    private fun getEndpoint(): String {
        return "${getBaseUrl()}/contacts/"
    }

    fun getContactsFromServer(context: Context, onErrorCallback: ((error: VolleyError) -> Unit)): MutableMap<Int, Pair<Boolean, ZonedDateTime>>{
        val requestQueue = ContactApiClient.getRequestQueue(context) ?: return HashMap()
        val responseFuture = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayRequest(getEndpoint(), responseFuture, Response.ErrorListener { onErrorCallback(it) })
        requestQueue.add(request)
        val test = responseFuture.get().toString()
        return parseContacts(test)
    }

    data class Contact(
        val userID: Int,
        val infectedUserID: Int
    )

    private fun parseContacts(contacts: String): MutableMap<Int, Pair<Boolean, ZonedDateTime>>{
        val parser: Parser = Parser.default()
        val parsed: JsonArray<*> = parser.parse(StringBuilder(contacts)) as JsonArray<*>
        val returnMap = mutableMapOf<Int, Pair<Boolean, ZonedDateTime>>()
        for(obj in parsed){
            val newInfectedUser = (obj as JsonObject)["userId"] as Int
            //val newInfectTimeStamp = (obj)
            val oldInfectedUser = obj["infectedUserId"] as Int
            val locationRecord = obj["locationRecord"] as JsonObject
            val timestampString = locationRecord["time"] as String
            val timestamp = ZonedDateTime.parse(timestampString)
            if(!returnMap.containsKey(newInfectedUser)){
                returnMap[newInfectedUser] = Pair(true, timestamp)
            }
            if(!returnMap.containsKey(oldInfectedUser)){
                returnMap[oldInfectedUser] = Pair(false, timestamp)
            }
        }
        return returnMap
    }
}