package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.AuthenticationCommunicator
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.TextViewUtils

class RegisterActivity : AppCompatActivity() {
    companion object {
        fun notifyUserOfProcess(ctx : Context){
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(ctx, "You successfully registered to save the world from corona!", duration).show()
            val settings = ctx.getSharedPreferences(Constants().CLIENT, Context.MODE_PRIVATE)
            settings.edit().putBoolean("registered", true).apply()
            val ctxActivity = ctx as Activity
            ctxActivity.finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerUser(this)
    }


    /*
    registers user on backend and creates jwt for requests
    will return true only if creating user on backend was succesful
     */
    private fun registerUser(ctx: Context) {
        val regBtn = findViewById<Button>(R.id.btn_register)
        val passwordTextView = findViewById<TextView>(R.id.et_password)
        val passwordReTextView = findViewById<TextView>(R.id.et_repassword)

        regBtn.setOnClickListener {
            val canContinue = TextViewUtils.checkMatchingPasswords(passwordTextView, passwordReTextView)
            if (canContinue) {
                val pw = passwordTextView.text.toString()
                AuthenticationCommunicator.createAndSaveUser(ctx, pw)
            }
        }
    }
}