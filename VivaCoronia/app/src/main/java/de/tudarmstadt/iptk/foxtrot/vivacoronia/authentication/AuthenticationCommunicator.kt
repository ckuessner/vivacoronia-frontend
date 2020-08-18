package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import org.json.JSONObject
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import de.tudarmstadt.iptk.foxtrot.vivacoronia.RegisterActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.ApiBaseClient
import java.util.*
import java.util.concurrent.ExecutionException

object AuthenticationCommunicator : ApiBaseClient(){

    class MatchingArraySizeException : Exception("The sizes of the arrays aren't the same")
    class UnsupportedTypeException : java.lang.Exception("You tried to save a datatype that isn't supported yet")

        private var TAG = "AuthenticationClient"

        /*
        Shows appropriate messages to user for different errors
         */
        fun handleErrorShowing(ctx: Context, errorCode: Int) {
            when(errorCode){
                Constants.NO_INTERNET -> Toast.makeText(ctx, "No internet", Toast.LENGTH_SHORT).show()
                Constants.AUTH_ERROR -> Toast.makeText(ctx, "Password was wrong, try again or go back", Toast.LENGTH_SHORT).show()
                Constants.VOLLEY_ERROR -> Toast.makeText(ctx, "Something went wrong with the server, oops", Toast.LENGTH_SHORT).show()
            }
        }

        // return Constants.AUTH_ERROR if error Code 401
        // return Constants.NULL_QUEUE if couldn't get requestQueue
        // return Constants.NO_INTERNET if no internet
        // return Constants.VOLLEY_ERROR for everything else
        private fun catchException(e : ExecutionException, isAdmin: Boolean = false) : Int{
            var toReturn = Constants.VOLLEY_ERROR
            if (VolleyError::class.java.isAssignableFrom(e.cause!!::class.java)){
                val error = e.cause as VolleyError
                if(error.networkResponse != null && isAdmin){
                    if(error.networkResponse.statusCode == 401){
                        toReturn = Constants.AUTH_ERROR
                    }
                }
                else if (NoConnectionError::class.java.isAssignableFrom(e.cause!!::class.java)){
                    toReturn = Constants.NO_INTERNET
                }
            }
            return toReturn
        }

        // this method creates a new JWT
        // and saves it in the preferences

        // return 0 iff everything ok
        // else return errorCode from catchException(...) above
        fun makeNewJWT(ctx: Context, password: String, userID: String, isAdmin : Boolean = false) : Int {
            val queue = getRequestQueue(ctx) ?: return Constants.NULL_QUEUE
            val baseUrl = Constants.SERVER_BASE_URL
            var url = "$baseUrl/user/$userID/login"
            if (isAdmin) {
                url = "$baseUrl/admin/login/"
            }
            val jsonPW = JSONObject()
            jsonPW.put("password", password)
            val responseFuture: RequestFuture<JSONObject> = RequestFuture.newFuture()


            val jsonPostRequest =
                object : JsonObjectRequest(url, jsonPW, responseFuture, responseFuture) {

                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    override fun getBody(): ByteArray {
                        return jsonPW.toString().toByteArray(Charsets.UTF_8)
                    }
                };
            queue.add(jsonPostRequest)

            try {
                val response = responseFuture.get()
                val jwt = response.opt("jwt")
                val savedContent = arrayOf<Any>(jwt, Date().getTime())
                var savedIdentifiers = arrayOf<String>(Constants.JWT, "jwt_timeCreated")
                if (isAdmin) {
                    savedIdentifiers = arrayOf<String>(Constants.adminJWT, "jwt_timeCreated")
                }
                //save retrieved jwt with creation time in preferences
                saveInPreferencesAny(ctx, savedIdentifiers, savedContent)
                return 0
            } catch (e: ExecutionException) {
                return catchException(e, isAdmin)
            }
        }

        /*
        this method saves every supported type of savedContent[i] (long, int, String) with the corresponding identifier (savedIdentifier[i])
         */
        private fun saveInPreferencesAny(
            ctx: Context,
            savedIdentifiers: Array<String>,
            savedContent: Array<Any>
        ) {
            var settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
            if (savedIdentifiers.size != savedContent.size) {
                throw MatchingArraySizeException()
            }
            // inspiration: https://github.com/android/android-ktx/issues/435
            for (i in savedContent.indices) {
                //this can be extended easily to other classes
                if (savedContent[i] is String) {
                    settings.edit().putString(savedIdentifiers[i], savedContent[i] as String)
                        .apply()
                }
                else if (savedContent[i] is Int)
                    settings.edit().putInt(savedIdentifiers[i], savedContent[i] as Int).apply()
                else if (savedContent[i] is Long)
                    settings.edit().putLong(savedIdentifiers[i], savedContent[i] as Long).apply()
                else
                    throw UnsupportedTypeException()
            }
        }

        //this will create a userID and save it in shared preferences
        fun createAndSaveUser(context: Context, pw: String): Int {
            val queue = getRequestQueue(context)?: return Constants.NULL_QUEUE
            val baseUrl = Constants.SERVER_BASE_URL
            // post URL, this URL creates new user id
            val url = "$baseUrl/user/"

            val jsonPW = JSONObject()
            jsonPW.put("password", pw)


            val response : RequestFuture<JSONObject> = RequestFuture.newFuture()
            val jsonRequest = object : JsonObjectRequest(Method.POST, url, jsonPW, response, response){
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return jsonPW.toString().toByteArray(Charsets.UTF_8)
                }
            };
            try {
                queue.add(jsonRequest)
                val responseWithUserID = response.get()
                val userID = responseWithUserID.opt("userId") as String
                saveInPreferences(context, userID)
                return 0
            }
            catch(e : ExecutionException){
                return catchException(e)
            }
        }
        /*
        This function is responsible to save the retrieved userID with the input password
        in shared preferences
         */
        private fun saveInPreferences(context: Context, response: String) {
            val settings = context.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
            settings.edit().putString("userID", response).apply()
        }
}