package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import org.json.JSONObject
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import de.tudarmstadt.iptk.foxtrot.vivacoronia.RegisterActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AuthenticationCommunicator {

    inner class MatchingArraySizeException : Exception("The sizes of the arrays aren't the same")
    inner class UnsupportedTypeException : java.lang.Exception("You tried to save a datatype that isn't supported yet")

    companion object {

        private var TAG = "AuthenticationClient"

        //this method is basically makeNewJWT except that it should only be used when user is registering
        //TODO: this is an ugly workaround, please refer to Patrick Vimr for refactoring ideas
        private fun makeFirstJWT(ctx: Context, pw: String){
            var timeNow = Date().time

            var settings = ctx.getSharedPreferences(Constants().CLIENT, Context.MODE_PRIVATE)
            val userID = settings.getString("userID", null)

            val queue = Volley.newRequestQueue(ctx)
            val baseUrl = Constants().SERVER_BASE_URL
            val url = "$baseUrl/userJWT/$userID/"

            val jsonPW = JSONObject()
            jsonPW.put("password", pw)
            val requestBody = jsonPW.toString()

            val jsonStringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    timeNow = Date().time - timeNow
                    // convert jsonObject Response to String
                    val respObject = JSONObject(response)
                    val jwt = respObject.opt("jwt") as String


                    // pack values to save in preferences
                    val savedContent = arrayOf<Any>(jwt, Date().getTime())
                    val savedIdentifiers = arrayOf<String>("jwt", "jwt_timeCreated")


                    // save jwt in client settings of user
                    saveInPreferencesAny(ctx, savedIdentifiers, savedContent)
                    RegisterActivity.notifyUserOfProcess(ctx)
                },
                Response.ErrorListener { error ->
                    Log.i(TAG, error.toString())
                    Toast.makeText(ctx, error.toString(), Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charsets.UTF_8)
                }
            };
            queue.add(jsonStringRequest)
        }

        // this method creates a new JWT
        // and saves it in the preferences
        fun makeNewJWT(ctx: Context, password: String?, userID: String? = null){
            val queue = Volley.newRequestQueue(ctx)
            val baseUrl = Constants().SERVER_BASE_URL
            var url = ""
            if (userID != null)
                url = "$baseUrl/userJWT/$userID/"
            else
                url = "$baseUrl/adminJWT/"
            val jsonPW = JSONObject()
            jsonPW.put("password", password)
            val responseFuture : RequestFuture<JSONObject> = RequestFuture.newFuture()


            val jsonPostRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonPW, responseFuture, Response.ErrorListener {
                Log.i(TAG, it.message ?: "something went wrong while taking jwt")
            }){};
            queue.add(jsonPostRequest)

            GlobalScope.launch {
                val response = responseFuture.get()
                val jwt = response.opt("jwt")
                val savedContent = arrayOf<Any>(jwt, Date().getTime())
                val savedIdentifiers = arrayOf<String>("jwt", "jwt_timeCreated")
                //save retrieved jwt with creation time in preferences
                saveInPreferencesAny(ctx, savedIdentifiers, savedContent)
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
            var settings = ctx.getSharedPreferences(Constants().CLIENT, Context.MODE_PRIVATE)
            if (savedIdentifiers.size != savedContent.size) {
                throw AuthenticationCommunicator().MatchingArraySizeException()
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
                    throw AuthenticationCommunicator().UnsupportedTypeException()
            }
        }

        //this will create a userID and save it in shared preferences
        fun createAndSaveUser(context: Context, pw: String): Boolean {
            var succReg = true
            val queue = Volley.newRequestQueue(context)
            val baseUrl = Constants().SERVER_BASE_URL
            // post URL, this URL creates new user id
            val url = "$baseUrl/user/"

            val jsonPW = JSONObject()
            jsonPW.put("password", pw)
            val requestBody = jsonPW.toString()

            // build request for creating a userid
            val jsonStringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    // convert jsonObject Response to String
                    val respObject = JSONObject(response)
                    val resUserID = respObject.opt("userId") as String


                    // save userID in settings
                    saveInPreferences(context, resUserID)
                    // make JWT for user
                    makeFirstJWT(context, pw)
                },
                Response.ErrorListener { error ->
                    succReg = false
                    Log.i(TAG, error.toString())
                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charsets.UTF_8)
                }
            };

            queue.add(jsonStringRequest)
            return succReg
        }
        /*
        This function is responsible to save the retrieved userID with the input password
        in shared preferences
         */
        private fun saveInPreferences(context: Context, response: String) {
            val settings = context.getSharedPreferences(Constants().CLIENT, Context.MODE_PRIVATE)
            settings.edit().putString("userID", response).apply()
        }
    }
}