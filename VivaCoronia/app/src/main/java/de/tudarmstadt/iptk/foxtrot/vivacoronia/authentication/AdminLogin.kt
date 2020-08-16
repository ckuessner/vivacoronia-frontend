package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AdminLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)
        setLoginLogic(this)
    }

    private fun setLoginLogic(ctx: Context){
        val loginBtn = findViewById<Button>(R.id.adminLogin)
        val firstPw = findViewById<TextView>(R.id.adminPw)
        val secondPw = findViewById<TextView>(R.id.adminPwRe)

        loginBtn.setOnClickListener {
            val canContinue = TextViewUtils.checkMatchingPasswords(firstPw, secondPw)
            if (canContinue){
                val pw = firstPw.text.toString()
                // we use empty userID because adminJWT doesn't need userID
                GlobalScope.launch {
                    val succJWT = AuthenticationCommunicator.makeNewJWT(ctx, pw, "", true)
                    runOnUiThread {
                        if(succJWT) finish()
                        else Toast.makeText(ctx, "Something went wrong, please check your internet and try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

}