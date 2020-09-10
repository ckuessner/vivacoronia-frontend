package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AuthenticationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //set progressbar not visible
        findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
        val isAdmin = intent.getBooleanExtra("isAdmin", false)
        val noAdminJWT = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.adminJWT, null) == null
        if(isAdmin && noAdminJWT){
            findViewById<TextView>(R.id.loginInfo).text = getString(R.string.admin_login_prompt)
        }
        else if (isAdmin && !noAdminJWT){
            findViewById<TextView>(R.id.loginInfo).text = getString(R.string.renew_admin_login_prompt)
        }
        setLoginLogic(this, isAdmin)
    }


    // set logic for login
    // if isAdmin is true, we want to get a new adminJWT, else userJWT
    // default is userJWT
    private fun setLoginLogic(ctx: Context, isAdmin : Boolean){
        val loginUser = findViewById<Button>(R.id.authButton)
        val passwordTextView = findViewById<TextView>(R.id.login_pw)

        loginUser.setOnClickListener{
            val canContinue = TextViewUtils.checkValidInput(passwordTextView)
            if(canContinue){
                runOnUiThread {
                    findViewById<ProgressBar>(R.id.loginProgress).visibility = View.VISIBLE
                }
                val pw = passwordTextView.text.toString()
                val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
                var succJWT = 0
                //check whether we wanna do admin login or normal login
                GlobalScope.launch {

                    succJWT = AuthenticationApiClient.makeNewJWT(ctx, pw, userID, isAdmin)
                    runOnUiThread {
                        if(succJWT == 0){
                            findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
                            Toast.makeText(ctx, getString(R.string.succLogin), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        else {
                            findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
                            RequestUtility.handleErrorShowing(ctx, succJWT)
                            //if we don't have admin rights, we don't need to keep the login
                            if(succJWT == Constants.FORBIDDEN)
                                finish()
                        }
                    }
                }
            }
        }
    }
}