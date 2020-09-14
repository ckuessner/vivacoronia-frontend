package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AuthenticationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text

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

        registerUser(this, false)
        loginAccountLogic(this)

    }

    private fun loginAccountLogic(ctx: Context){
        val loginCheckBox = findViewById<CheckBox>(R.id.existingAccountCheckbox)
        val regBtn = findViewById<Button>(R.id.btn_register)
        val passwordReTextView = findViewById<TextView>(R.id.et_repassword)
        val userIDTextView = findViewById<TextView>(R.id.register_userID)
        loginCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                regBtn.text = getString(R.string.loginReg)
                //make second pw not appear anymore
                passwordReTextView.visibility = View.GONE
                passwordReTextView.isClickable = false
                //make userIDTextAppear and editable
                userIDTextView.visibility = View.VISIBLE
                userIDTextView.inputType = InputType.TYPE_CLASS_TEXT
                userIDTextView.isClickable = true
                //set logic of login/register button to login instead of registering
                registerUser(ctx, true)
            }
            else if (!isChecked){
                //same as above but opposite
                regBtn.text = getString(R.string.register)
                passwordReTextView.visibility = View.VISIBLE
                passwordReTextView.isClickable = true
                userIDTextView.visibility = View.INVISIBLE
                userIDTextView.isClickable = false
                registerUser(ctx, false)
            }

        }

    }
    
    private fun doRegisterProcess(ctx: Context) {
        val passwordTextView = findViewById<TextView>(R.id.et_password)
        val passwordReTextView = findViewById<TextView>(R.id.et_repassword)
        val canContinue = TextViewUtils.checkMatchingPasswords(passwordTextView, passwordReTextView)
        if (canContinue) {
            val pw = passwordTextView.text.toString()
            GlobalScope.launch {
                val creationSucc = AuthenticationApiClient.createAndSaveUser(ctx, pw)
                var jwtDone = 0
                if (creationSucc == 0) {
                    //since we only get useriD == 0 if everything was ok, we can safely cast to string
                    val userID =
                        ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(
                            Constants.USER_ID, null
                        ) as String
                    jwtDone = AuthenticationApiClient.makeNewJWT(ctx, pw, userID)
                    runOnUiThread {
                        if (jwtDone == 0) {
                            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
                            showEmailDialog(ctx, userID)
                            //finishRegister(ctx)
                        }
                        else {
                            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
                            RequestUtility.handleErrorShowing(ctx, jwtDone)
                        }
                    }
                } else
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
                        RequestUtility.handleErrorShowing(ctx, creationSucc)
                    }
            }
        }
        else {
            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
        }
    }

    private fun showEmailDialog(ctx: Context, userID: String){
        val builder = AlertDialog.Builder(ctx, R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.EmailTitle))
        builder.setMessage(getString(R.string.EmailInfo))
        val inputEmail = EditText(ctx)
        inputEmail.setHint(R.string.EmailHint)
        builder.setView(inputEmail)

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            try {
                val subject = getString(R.string.Email_Subject)
                val body = Uri.encode( "Your UserId is: $userID")
                val data = Uri.parse("mailto:${inputEmail.text}?subject=$subject&body=$body")
                val emailIntent = Intent(Intent.ACTION_VIEW)
                emailIntent.data = data
                startActivityForResult(emailIntent, 0)
            }
            catch(e: ActivityNotFoundException){
                Toast.makeText(ctx, getString(R.string.emailFailInfo), Toast.LENGTH_SHORT).show()
                finishRegister(ctx)
            }
        }
        builder.setNegativeButton(android.R.string.no) {_, _ ->
            Toast.makeText(ctx, getString(R.string.emailNotSendInfo), Toast.LENGTH_SHORT).show()
            finishRegister(ctx)
        }
        val dialog = builder.create()
        dialog.show()
        //init positive button to not be clickable until email valid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        inputEmail.doAfterTextChanged {txt ->
            val canContinue = TextViewUtils.checkValidInput(txt.toString(), true)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = canContinue

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finishRegister(this)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun doLoginProcess(ctx: Context) {
        val userID = findViewById<TextView>(R.id.register_userID)
        val password = findViewById<TextView>(R.id.et_password)
        val canContinue = TextViewUtils.checkValidInput(password) && TextViewUtils.checkValidInput(userID)
        if(canContinue){
            GlobalScope.launch {
                val jwtDone = AuthenticationApiClient.makeNewJWT(ctx, password.text.toString(), userID.text.toString())
                if(jwtDone == 0){
                    RequestUtility.saveInPreferences(ctx, userID.text.toString())
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
                        finishRegister(ctx)
                    }
                }
                else {
                    runOnUiThread {
                        findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
                        RequestUtility.handleErrorShowing(ctx, jwtDone, true)
                    }
                }
            }
        }
        else
            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.GONE
    }

    private fun setButtonLogic(btn: Button, isRegister: Boolean, ctx: Context){
    if(isRegister){
        btn.setOnClickListener {
            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.VISIBLE
            doRegisterProcess(ctx)
        }
    }
    else {
        btn.setOnClickListener {
            findViewById<ProgressBar>(R.id.registerProgress).visibility = View.VISIBLE
            doLoginProcess(ctx)
            }
        }
    }

/*
registers user on backend and creates jwt for requests
will return true only if creating user on backend was succesful
*/
private fun registerUser(ctx: Context, isLogin : Boolean) {
val regBtn = findViewById<Button>(R.id.btn_register)

if(!isLogin)
    setButtonLogic(regBtn, true, ctx)
else
    setButtonLogic(regBtn, false, ctx)
}
}