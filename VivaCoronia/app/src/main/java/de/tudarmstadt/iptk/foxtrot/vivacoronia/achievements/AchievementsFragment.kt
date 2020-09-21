package de.tudarmstadt.iptk.foxtrot.vivacoronia.achievements

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AchievementApiClient
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
            val achievementInfoNew = AchievementApiClient.getAchievementInformation(ctx)
            //if above value is null, we load the old settings for achievements
            if (achievementInfoNew == null) {
                val achievements = db.coronaDao().getAllAchievements()
                for (achievement: AchievementInfo in achievements) {
                    requireActivity().runOnUiThread {
                        setAchievementInfo(view, achievement)
                    }
                }
            } else {
                //else we can iterate through all achievements and load new achievement possibly
                for (achievement: AchievementInfo in achievementInfoNew) {
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
        val idAsString = convertAchievementToID(achievement) ?: return
        val id = view?.resources?.getIdentifier(idAsString, "id", requireActivity().applicationContext.packageName) as Int
        val toUpdate = view.findViewById<ImageView>(id)
        toUpdate.alpha = 1.0f
    }

    private fun convertAchievementToID(achievement: AchievementInfo) : String?{
        val type = achievement.achievement
        val tier = achievement.type
        val startString = "imgView"


        return "$startString$type$tier"

    }

    private fun updateDB(achievement : AchievementInfo, db: AppDatabase){
        db.coronaDao().updateAchievement(achievement)
    }

    private fun updateInfectionScore(ctx: Context, view: View?){
        GlobalScope.launch {
            val infectionScore = AchievementApiClient.getInfectionScore(ctx)

            // if we get -1.0 we didn't get a new value due to any kind of error so we just load the old value
            if (infectionScore == -1.0f) {
                val score = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
                    .getFloat(Constants.INFECTION_SCORE, 0.0f).toString()
                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.textViewInfectionScore)?.text = score
                }
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