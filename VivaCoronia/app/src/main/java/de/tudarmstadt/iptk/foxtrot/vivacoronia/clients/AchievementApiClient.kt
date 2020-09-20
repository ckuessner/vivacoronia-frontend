package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import com.android.volley.toolbox.RequestFuture
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ExecutionException

object AchievementApiClient : ApiBaseClient() {
    private fun getEndPoint(ctx: Context, endpoint: String) : String{
        val userID = getUserId(ctx)
        return "${getBaseUrl()}/user/$userID/$endpoint"
    }

    //this method assumes valid return value given by request
    private fun createAchievementInfo(jsonArr : JSONArray) : ArrayList<Constants.AchievementStruct> {
        val result = ArrayList<Constants.AchievementStruct>()

        //iterate over all entries and get status info and make array
        for(i in 0 until jsonArr.length()){
            val currObj = jsonArr.getJSONObject(i)
            val name = currObj.opt("name") as String
            val type = makeBadgeTier(currObj.opt("badge") as String)
            val remaining = currObj.opt("remaining") as Int
            val howMany = currObj.opt("howmany") as Int
            result.add( Constants.AchievementStruct(name, type, remaining, howMany))
        }
        return result
    }

    private fun makeBadgeTier(tier: String) : Constants.BadgeType{
        when(tier) {
            "none" -> return Constants.BadgeType.NONE
            "bronze" -> return Constants.BadgeType.BRONZE
            "silver" -> return Constants.BadgeType.SILVER
            "gold" -> return Constants.BadgeType.GOLD
            else -> return Constants.BadgeType.NONE
        }
    }

    fun getAchievementInformation(ctx: Context) : ArrayList<Constants.AchievementStruct>? {
        val requestQueue = getRequestQueue(ctx) ?: return null
        val responseFuture = RequestFuture.newFuture<JSONArray>()
        val url = getEndPoint(ctx, Constants.ENDPOINT_ACHIEVEMENT)

        val request = JsonArrayJWT(url, responseFuture, responseFuture, ctx)
        requestQueue.add(request)
        return try {
            val achievementStatusList = responseFuture.get()
            createAchievementInfo(achievementStatusList)
        } catch(e: ExecutionException){
            val errorCode = RequestUtility.catchException(e)
            RequestUtility.handleErrorShowing(ctx, errorCode)
            null
        }
    }

    /*
    get infection score of user, is -1.0 then there was an error beforehand
     */
    fun getInfectionScore(ctx: Context) : Double {
        val requestQueue = getRequestQueue(ctx) ?: return -1.0
        val responseFuture = RequestFuture.newFuture<JSONObject>()
        val url = getEndPoint(ctx, Constants.ENDPOINT_SCORE)

        val request = JsonObjectJWT(url, null, responseFuture, responseFuture ,ctx)
        requestQueue.add(request)
        return try {
            responseFuture.get().opt("infectionScore") as Double
        } catch(e: ExecutionException){
            val errorCode = RequestUtility.catchException(e)
            RequestUtility.handleErrorShowing(ctx, errorCode)
            -1.0
        }

    }
}