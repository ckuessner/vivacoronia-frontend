package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val ctx : Context = this
        // user is registered if userID is not null
        val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null)
        if (userID != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else {
            val intent = Intent("registerApplication.intent.action.Launch")
            startActivity(intent)
        }


    }
}