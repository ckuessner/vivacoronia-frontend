package de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.RegisterActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.LoginActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        val ctx : Context = this
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
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
}