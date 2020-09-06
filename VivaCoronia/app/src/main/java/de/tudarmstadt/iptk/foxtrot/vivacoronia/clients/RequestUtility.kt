package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.widget.Toast
import com.android.volley.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.util.concurrent.ExecutionException

object RequestUtility : ApiBaseClient(){

    class MatchingArraySizeException : Exception("The sizes of the arrays aren't the same")
    class UnsupportedTypeException : java.lang.Exception("You tried to save a datatype that isn't supported yet")

        private var TAG = "AuthenticationClient"

        /*
        Shows appropriate messages to user for different errors
         */
        fun handleErrorShowing(ctx: Context, errorCode: Int) {
            when(errorCode){
                Constants.NO_INTERNET -> Toast.makeText(ctx, ctx.getString(R.string.noInternet), Toast.LENGTH_SHORT).show()
                Constants.AUTH_ERROR -> Toast.makeText(ctx, ctx.getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show()
                Constants.SERVER_ERROR -> Toast.makeText(ctx, ctx.getString(R.string.serverError), Toast.LENGTH_SHORT).show()
                Constants.FIREWALL_ERROR -> Toast.makeText(ctx, ctx.getString(R.string.firewallError), Toast.LENGTH_SHORT).show()
                Constants.FORBIDDEN -> Toast.makeText(ctx, ctx.getString(R.string.forbiddenError), Toast.LENGTH_SHORT).show()
            }
        }

        // return Constants.AUTH_ERROR if error Code 401
        // return Constants.NULL_QUEUE if couldn't get requestQueue
        // return Constants.NO_INTERNET if no internet
        // return Constants.VOLLEY_ERROR for everything else
        fun catchException(e : ExecutionException, isAdmin: Boolean = false) : Int{
            var toReturn = Constants.VOLLEY_ERROR
            if (VolleyError::class.java.isAssignableFrom(e.cause!!::class.java)){
                when(e.cause as VolleyError){
                    is ServerError -> toReturn = Constants.SERVER_ERROR
                    //https://stackoverflow.com/questions/31802105/what-exactly-does-volley-volleyerror-networkerror-mean-in-android
                    is NoConnectionError -> toReturn = Constants.NO_INTERNET
                    is NetworkError -> toReturn = Constants.FIREWALL_ERROR
                }
                val volleyE = e.cause as VolleyError
                if(volleyE.networkResponse.statusCode == 403){
                    toReturn == Constants.FORBIDDEN
                }
                else if (volleyE is AuthFailureError && volleyE.networkResponse.statusCode == 401){
                    toReturn = Constants.AUTH_ERROR
                }
            }
            return toReturn
        }


        /*
        this method saves every supported type of savedContent[i] (long, int, String) with the corresponding identifier (savedIdentifier[i])
         */
        fun saveInPreferencesAny(
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
                else if (savedContent[i] is Boolean){
                    settings.edit().putBoolean(savedIdentifiers[i], savedContent[i] as Boolean).apply()
                }
                else if (savedContent[i] is Int)
                    settings.edit().putInt(savedIdentifiers[i], savedContent[i] as Int).apply()
                else if (savedContent[i] is Long)
                    settings.edit().putLong(savedIdentifiers[i], savedContent[i] as Long).apply()
                else
                    throw UnsupportedTypeException()
            }
        }

        /*
        This function is responsible to save the retrieved userID with the input password
        in shared preferences
         */
        fun saveInPreferences(context: Context, response: String) {
            val settings = context.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
            settings.edit().putString("userID", response).apply()
        }
}