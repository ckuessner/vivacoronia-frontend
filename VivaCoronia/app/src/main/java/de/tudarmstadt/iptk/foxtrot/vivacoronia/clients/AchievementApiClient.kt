package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.RequestFuture
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.AchievementInfo
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.concurrent.ExecutionException

object AchievementApiClient : ApiBaseClient() {
    class WrongSyntaxForAchievment(message: String) : Exception(message)

    private fun getEndPoint(ctx: Context, endpoint: String) : String{
        val userID = getUserId(ctx)
        return "${getBaseUrl()}/user/$userID/$endpoint"
    }

    //this method assumes valid return value given by request
    private fun createAchievementInfo(jsonArr : JSONArray) : ArrayList<AchievementInfo> {
        val result = ArrayList<AchievementInfo>()

        //iterate over all entries and get status info and make array
        for(i in 0 until jsonArr.length()){
            val currObj = jsonArr.getJSONObject(i)
            val achievementType = convertValidAchievId(currObj.opt("name") as String)
            val type = convertTypeValidId(currObj.opt("badge") as String)
            if(type == null|| achievementType == null)
                throw WrongSyntaxForAchievment("Either the badge or the achievement itself couldn't be parsed")
            val remaining = currObj.opt("remaining") as Int
            val howMany = currObj.opt("howmany") as Int
            result.add( AchievementInfo(achievementType, type, remaining, howMany))
        }
        return result
    }

    private fun convertTypeValidId(requestBadge : String) : String?{
        when(requestBadge){
            "bronce" -> return Constants.BADGE_BRONZE
            "silver" -> return Constants.BADGE_SILVER
            "gold" -> return Constants.BADGE_SILVER
            "none" -> return Constants.BADGE_NONE
            else -> return null
        }
    }
    private fun convertValidAchievId(requestAchiev: String) : String?{
        when(requestAchiev){
            "zombie" -> return Constants.ACHIEVEMENT_ZOMBIE
            "foreveralone" -> return Constants.ACHIEVEMENT_ALONE
            "moneyboy" -> return Constants.ACHIEVEMENT_MONEYBOY
            "hamsterbuyer" -> return Constants.ACHIEVEMENT_HAMSTERBUYER
            "superspreader" -> return Constants.ACHIEVEMENT_SUPERSPREADER
            "quizmaster" -> return Constants.ACHIEVEMENT_QUIZMASTER
            else -> return null
        }
    }

    fun getAchievementInformation(ctx: Context) : ArrayList<AchievementInfo>? {
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
    fun getInfectionScore(ctx: Context) : Float {
        val requestQueue = getRequestQueue(ctx) ?: return -1.0f
        val responseFuture = RequestFuture.newFuture<JSONObject>()
        val url = getEndPoint(ctx, Constants.ENDPOINT_SCORE)

        val request = JsonObjectJWT(url, null, responseFuture, responseFuture ,ctx)
        requestQueue.add(request)
        var scoreAsFloat = 0.0f
        val score = responseFuture.get().opt("infectionScore")
        if(score != 0)
            scoreAsFloat = score as Float
        return try {
            scoreAsFloat
        } catch(e: ExecutionException){
            val errorCode = RequestUtility.catchException(e)
            RequestUtility.handleErrorShowing(ctx, errorCode)
            -1.0f
        }

    }
}