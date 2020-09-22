package de.tudarmstadt.iptk.foxtrot.vivacoronia.achievements

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.LoginActivity
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
        setInfoListeners(view, database)
        updateAchievementStatus(requireActivity(), view, database)
        updateInfectionScore(requireActivity(), view)
        // Inflate the layout for this fragment
        return view
    }

    private fun setInfoListeners(view : View, db : AppDatabase){
        val infectionInfo = view.findViewById<ImageView>(R.id.infoInfectionscore)
        val achievementInfos = ArrayList<Pair<ImageView, String>>()
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoZombie), Constants.ACHIEVEMENT_ZOMBIE))
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoSuperspreader), Constants.ACHIEVEMENT_SUPERSPREADER))
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoMoneyboy), Constants.ACHIEVEMENT_MONEYBOY))
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoHamsterbuyer), Constants.ACHIEVEMENT_HAMSTERBUYER))
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoQuiz), Constants.ACHIEVEMENT_QUIZMASTER))
        achievementInfos.add(Pair(view.findViewById<ImageView>(R.id.infoForeveralone), Constants.ACHIEVEMENT_ALONE))


        infectionInfo.setOnClickListener {
            requireActivity().runOnUiThread {
                makeAlertDialog(getString(R.string.infectionScoreInfo))
            }
        }

        for(info: Pair<ImageView, String> in achievementInfos){
            GlobalScope.launch {
                val currAchievement = db.coronaDao().getAchievement(info.second)
                val infoImage = info.first
                val achievmentInfo = getInfoAboutAchievement(info.second)
                requireActivity().runOnUiThread {
                    val basicInfoAchievment = getInfoAboutAchievement(info.second)
                    val neededForHigher = currAchievement.neededForHigher
                    val neededInfoAchievement = makeNeededInfo(info.second, neededForHigher)
                    val percentageOfPeople = currAchievement.percentageOfPeople
                    val percentageInfoAchievement =
                        makePercentageInfo(info.second, percentageOfPeople)
                    val resultString =
                        "$basicInfoAchievment\n$neededInfoAchievement\n$percentageInfoAchievement"
                    infoImage.setOnClickListener {
                        makeAlertDialog(resultString)
                    }
                }
            }
        }
    }

    private fun makeAlertDialog(toShow: String){
        val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.generalInfo))
        builder.setMessage(toShow)
        // needs empty positive button to work
        builder.setPositiveButton(android.R.string.yes){ _, _ ->
        }
        builder.show()
    }

    private fun makePercentageInfo(achievement: String, percentage : Int) : String{
        //if percentage is 0, we dont need to show that 0% percent of people achieved this badge
        if(percentage == 0)
            return ""
        var achievName = ""
        when(achievement){
            Constants.ACHIEVEMENT_ALONE -> achievName =  "Forever Alone"
            Constants.ACHIEVEMENT_SUPERSPREADER -> achievName = "Superspreader"
            Constants.ACHIEVEMENT_QUIZMASTER -> achievName = "Quizmaster"
            Constants.ACHIEVEMENT_HAMSTERBUYER -> achievName = "Hamsterbuyer"
            Constants.ACHIEVEMENT_ZOMBIE -> achievName = "Zombie"
            Constants.ACHIEVEMENT_MONEYBOY-> achievName = "Moneyboy"
        }
        return "$percentage% of people unlocked the current badge of the achievement \"$achievName\" "
    }

    private fun makeNeededInfo(achievement : String, neededForHigher : Int) : String{
        if(neededForHigher == 0)
            return ""
        when(achievement){
            Constants.ACHIEVEMENT_ALONE -> return "You need to be $neededForHigher days alone to unlock the next badge!"
            Constants.ACHIEVEMENT_SUPERSPREADER -> return "Infect $neededForHigher more people to unlock the next badge (please don't!)"
            Constants.ACHIEVEMENT_QUIZMASTER -> return "Win $neededForHigher more quizzes correctly to unlock the next badge!"
            Constants.ACHIEVEMENT_HAMSTERBUYER -> return "Buy $neededForHigher more items to unlock the next badge!"
            Constants.ACHIEVEMENT_ZOMBIE -> return "Walk $neededForHigher more kilometres to unlock the next badge! (or be a nice person and stay at home!)"
            Constants.ACHIEVEMENT_MONEYBOY-> return "Sell $neededForHigher more items to unlock the next badge and someday be the next Jeff Bezos!"
            else -> return ""
        }
    }

    private fun getInfoAboutAchievement(achievementName: String): String{
        when(achievementName){
            Constants.ACHIEVEMENT_ALONE -> return getString(R.string.aloneInfo)
            Constants.ACHIEVEMENT_SUPERSPREADER -> return getString(R.string.spreaderInfo)
            Constants.ACHIEVEMENT_QUIZMASTER -> return getString(R.string.quizInfo)
            Constants.ACHIEVEMENT_HAMSTERBUYER -> return getString(R.string.hamsterInfo)
            Constants.ACHIEVEMENT_ZOMBIE -> return getString(R.string.zombieInfo)
            Constants.ACHIEVEMENT_MONEYBOY-> return getString(R.string.moneyInfo)
            else -> return ""
        }
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