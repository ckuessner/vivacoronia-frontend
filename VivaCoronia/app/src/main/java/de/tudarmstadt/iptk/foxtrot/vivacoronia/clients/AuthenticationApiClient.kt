package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutionException

object AuthenticationApiClient : ApiBaseClient() {

    fun checkStatus(ctx: Context, userID: String) : Int {
        val queue = RequestUtility.getRequestQueue(ctx) ?: return Constants.NULL_QUEUE
        val baseUrl = Constants.SERVER_BASE_URL
        var url = "$baseUrl/admin/$userID/login/"

        val responseFuture: RequestFuture<JSONObject> = RequestFuture.newFuture()
        val jsonRequest = object : JsonObjectRequest(url, null,responseFuture, responseFuture){}
        queue.add(jsonRequest)
        try {
            val response = responseFuture.get()
            val newStatus = response.opt("isAdmin")
            val savedContent = arrayOf<Any>(newStatus)
            val savedIdentifiers = arrayOf<String>(Constants.IS_ADMIN)
            RequestUtility.saveInPreferencesAny(ctx, savedIdentifiers, savedContent)
            return 0
        }
        catch(e: ExecutionException){
            return RequestUtility.catchException(e, false)
        }
    }

    // this method creates a new JWT
    // and saves it in the preferences
    // return 0 iff everything ok
    // else return errorCode from catchException(...) above
    fun makeNewJWT(ctx: Context, password: String, userID: String, isAdmin : Boolean = false) : Int {
        val queue = RequestUtility.getRequestQueue(ctx) ?: return Constants.NULL_QUEUE
        val baseUrl = Constants.SERVER_BASE_URL
        var url = "$baseUrl/user/$userID/login"
        if (isAdmin) {
            url = "$baseUrl/admin/$userID/login/"
        }
        val jsonPW = JSONObject()
        jsonPW.put("password", password)
        val responseFuture: RequestFuture<JSONObject> = RequestFuture.newFuture()


        val jsonPostRequest =
            object : JsonObjectRequest(url, jsonPW, responseFuture, responseFuture) {};
        queue.add(jsonPostRequest)

        try {
            val response = responseFuture.get()
            val jwt = response.opt("jwt")
            val savedContent = arrayOf<Any>(jwt, Date().time)
            var savedIdentifiers = arrayOf<String>(Constants.JWT, "jwt_timeCreated")
            if (isAdmin) {
                savedIdentifiers = arrayOf<String>(Constants.adminJWT, Constants.adminJWT_Time)
            }
            //save retrieved jwt with creation time in preferences
            RequestUtility.saveInPreferencesAny(ctx, savedIdentifiers, savedContent)
            return 0
        } catch (e: ExecutionException) {
            return RequestUtility.catchException(e, isAdmin)
        }
    }

    //this will create a userID and save it in shared preferences
    fun createAndSaveUser(context: Context, pw: String): Int {
        val queue = RequestUtility.getRequestQueue(context) ?: return Constants.NULL_QUEUE
        val baseUrl = Constants.SERVER_BASE_URL
        // post URL, this URL creates new user id
        val url = "$baseUrl/user/"

        val jsonPW = JSONObject()
        jsonPW.put("password", pw)


        val response : RequestFuture<JSONObject> = RequestFuture.newFuture()
        val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonPW, response, response){};
        try {
            queue.add(jsonRequest)
            val responseWithUserID = response.get()
            val userID = responseWithUserID.opt("userId") as String
            RequestUtility.saveInPreferences(context, userID)
            return 0
        }
        catch(e : ExecutionException){
            return RequestUtility.catchException(e)
        }
    }

}