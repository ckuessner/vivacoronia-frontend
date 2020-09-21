package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.RegisterActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.LoginActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.AchievementInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val ctx : Context = this
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)

        val isInit = settings.getBoolean("db_initialized", false)
        //init database with empty achievements
        if(!isInit){
            val db = AppDatabase.getDatabase(this)
            GlobalScope.launch {
                initAchievements(db, settings)
            }
        }
        // user is registered if userID is not null
        val userID = settings.getString(Constants.USER_ID, null)
        if (userID == null){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        // because we want to get back to the main activity after login, we first start main and then login
        // otherwise finish in the login Activity would return to nothing, since we finish the startActivity
        else if(settings.getString(Constants.JWT, null) == null){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*
    this method initializes the database for all achievements in Enumeration Constants.Achievement
     */
    private fun initAchievements(db: AppDatabase, settings: SharedPreferences){
        settings.edit().putBoolean("db_initialized", true).apply()
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_QUIZMASTER, Constants.BADGE_NONE, 100, 0))
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_SUPERSPREADER, Constants.BADGE_NONE, 100, 0))
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_HAMSTERBUYER, Constants.BADGE_NONE, 100, 0))
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_MONEYBOY, Constants.BADGE_NONE, 100, 0))
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_ALONE, Constants.BADGE_NONE, 100, 0))
        db.coronaDao().initAchievement(AchievementInfo(Constants.ACHIEVEMENT_ZOMBIE, Constants.BADGE_NONE, 100, 0))


    }
}