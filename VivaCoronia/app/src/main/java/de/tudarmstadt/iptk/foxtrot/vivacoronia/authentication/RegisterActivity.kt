package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.AuthenticationCommunicator
import de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication.TextViewUtils
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    companion object {
        fun finishRegister(ctx : Context){
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(ctx, "You successfully registered to save the world from corona!", duration).show()
            val ctxActivity = ctx as Activity
            ctxActivity.finish()
            val intent = Intent(ctx, MainActivity::class.java)
            ctx.startActivity(intent)

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
                GlobalScope.launch {
                    val userID = AuthenticationCommunicator.createAndSaveUser(ctx, pw)
                    var jwtDone = false
                    if(userID != null) {
                        jwtDone = AuthenticationCommunicator.makeNewJWT(ctx, pw, userID)
                        runOnUiThread {
                            if (jwtDone) finishRegister(ctx)
                            else
                                Toast.makeText(ctx, "Something went wrong while creating your account, check internet and try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        runOnUiThread {
                            Toast.makeText(
                                ctx,
                                "Something went wrong while creating your account, check internet and try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}