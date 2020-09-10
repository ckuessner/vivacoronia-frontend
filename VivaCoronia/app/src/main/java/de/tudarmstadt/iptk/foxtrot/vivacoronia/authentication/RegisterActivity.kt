package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AuthenticationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private fun finishRegister(ctx : Context){
            val duration = Toast.LENGTH_SHORT
            Toast.makeText(ctx, getString(R.string.succRegister), duration).show()
            val ctxActivity = ctx as Activity
            ctxActivity.finish()
            val intent = Intent(ctx, MainActivity::class.java)
            ctx.startActivity(intent)

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
                    val creationSucc = AuthenticationApiClient.createAndSaveUser(ctx, pw)
                    if(creationSucc == 0) {
                        //since we only get useriD == 0 if everything was ok, we can safely cast to string
                        val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(
                            Constants.USER_ID, null) as String
                        val jwtDone = AuthenticationApiClient.makeNewJWT(ctx, pw, userID)
                        runOnUiThread {
                            if (jwtDone == 0) finishRegister(ctx)
                            else
                                RequestUtility.handleErrorShowing(ctx, jwtDone)
                        }
                    }
                    else
                        runOnUiThread {
                            RequestUtility.handleErrorShowing(ctx, creationSucc)
                        }
                }
            }
        }
    }
}