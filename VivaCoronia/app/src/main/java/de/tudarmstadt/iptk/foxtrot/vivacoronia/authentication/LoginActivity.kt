package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.TextView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.AuthenticationCommunicator

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setLoginLogic(this)
    }


    private fun setLoginLogic(ctx: Context){
        val loginUser = findViewById<Button>(R.id.authButton)
        val passwordTextView = findViewById<TextView>(R.id.login_pw)
        val passwordReTextView = findViewById<TextView>(R.id.login_pwRe)

        loginUser.setOnClickListener{
            val canContinue = TextViewUtils.checkMatchingPasswords(passwordTextView, passwordReTextView)
            if(canContinue){
                val pw = passwordTextView.text.toString()
                val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
                AuthenticationCommunicator.makeNewJWT(ctx, pw, userID)
                finish()
            }
        }
    }
}