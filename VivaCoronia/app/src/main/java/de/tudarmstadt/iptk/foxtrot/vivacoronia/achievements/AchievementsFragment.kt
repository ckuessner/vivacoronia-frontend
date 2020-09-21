package de.tudarmstadt.iptk.foxtrot.vivacoronia.achievements

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AchievementApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.AchievementInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "AchievementFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [AchievementsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AchievementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)
        val database = AppDatabase.getDatabase(requireActivity())
        updateAchievementStatus(requireActivity(), view, database)
        updateInfectionScore(requireActivity(), view)
        // Inflate the layout for this fragment
        return view
    }

    private fun updateAchievementStatus(ctx: Context, view: View?, db: AppDatabase){
        GlobalScope.launch {
            val achievementInfoRequest = AchievementApiClient.getAchievementInformation(ctx)
            val achievementInfoNew = achievementInfoRequest.first
            val achievemenRequestSucceed = achievementInfoRequest.second
            //if above value is null, we load the old settings for achievements
            if (achievemenRequestSucceed != 0) {
                val achievements = db.coronaDao().getAllAchievements()
                for (achievement: AchievementInfo in achievements) {
                    requireActivity().runOnUiThread {
                        RequestUtility.handleErrorShowing(ctx, achievemenRequestSucceed)
                        setAchievementInfo(view, achievement)
                    }
                }
            } else {
                //since we can only get in this case if we have succeeded and thus get non null list, we can safely cast
                val achievementList = achievementInfoNew as ArrayList<AchievementInfo>
                //else we can iterate through all achievements and load new achievement possibly
                for (achievement: AchievementInfo in achievementList) {
                    requireActivity().runOnUiThread {
                        setAchievementInfo(view, achievement)
                    }
                    updateDB(achievement, db)
                }
            }
        }
    }

    private fun setAchievementInfo(view: View?, achievement: AchievementInfo){
        if(achievement.type == Constants.BADGE_NONE)
            return
        var amountBadgesToShow = convertBadgeTypeToAmount(achievement.type)
        val idsAsString = arrayListOf<String>()
        while (amountBadgesToShow != 0){
            val badgeIDType = calculateNeededBadges(amountBadgesToShow).second
            idsAsString.add(convertAchievementToID(achievement, badgeIDType))
            amountBadgesToShow--
        }

        for(idString in idsAsString){
            Log.i("achiev", idString)
            val id = view?.resources?.getIdentifier(idString, "id", requireActivity().applicationContext.packageName) as Int
            val toUpdate = view.findViewById<ImageView>(id)
            toUpdate.alpha = 1.0f
        }
    }

    private fun convertBadgeTypeToAmount(badgeType : String) : Int{
        when(badgeType){
            "Bronce" -> return 1
            "Silver" -> return 2
            "Gold" -> return 3
            else -> return 0
        }
    }

    private fun calculateNeededBadges(badgeType: Int) : Pair<Int, String> {
        when(badgeType){
            1 -> return Pair(1, Constants.BADGE_BRONZE)
            2 -> return Pair(2, Constants.BADGE_SILVER)
            3 -> return Pair(3, Constants.BADGE_GOLD)
            0 -> return Pair(0, Constants.BADGE_NONE)
            else -> return Pair(0, Constants.BADGE_NONE)
        }
    }

    private fun convertAchievementToID(achievement: AchievementInfo, badgeTier: String) : String{
        val type = achievement.achievement
        val startString = "imgView"
        return "$startString$type$badgeTier"

    }

    private fun updateDB(achievement : AchievementInfo, db: AppDatabase){
        db.coronaDao().updateAchievement(achievement)
    }

    private fun updateInfectionScore(ctx: Context, view: View?){
        GlobalScope.launch {
            val infectionRequestPair = AchievementApiClient.getInfectionScore(ctx)
            val infectionScore = infectionRequestPair.first
            val requestSucceed = infectionRequestPair.second

            // if we get != 0, we have an error that we have to handle and we show the error and the old saved state of achievements
            if(requestSucceed != 0){
                val score = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
                    .getFloat(Constants.INFECTION_SCORE, 0.0f).toString()
                requireActivity().runOnUiThread {
                    RequestUtility.handleErrorShowing(ctx, requestSucceed)
                    view?.findViewById<TextView>(R.id.textViewInfectionScore)?.text = score
                }
                return@launch
            }
            // else we got a valid number that we can use to first set our new infection score and then save it in preferences
            else {
                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.textViewInfectionScore)?.text =
                        infectionScore.toString()
                }
                ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).edit()
                    .putFloat(Constants.INFECTION_SCORE, infectionScore).apply()
            }
        }
    }
}