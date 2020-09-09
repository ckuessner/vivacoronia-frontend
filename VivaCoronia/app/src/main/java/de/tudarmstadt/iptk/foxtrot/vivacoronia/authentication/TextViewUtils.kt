package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.util.Patterns
import android.widget.TextView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

object TextViewUtils {

    /*
    this only checks whether input isn't empty
     */
    fun checkValidInput(textView: TextView, isEmail : Boolean = false):Boolean {
        var mode = true
        if (textView.text.isEmpty()) {
            mode = false
        }
        if(isEmail && mode){
            val pattern = Patterns.EMAIL_ADDRESS
            mode = pattern.matcher(textView.text.toString()).matches()
        }

        setModeOnTextView(textView, "Invalid Input", mode)

        return mode
    }

    private fun setModeOnTextView(textView: TextView, msg:String, mode:Boolean) {
        if (mode) {
            textView.setBackgroundResource(R.drawable.et_custom)
        }
        else {
            textView.error = msg
            textView.setBackgroundResource(R.drawable.et_err)
        }
    }

    /*
 check whether input of both textfelds match and only if they do the register process can continue
  */
    fun checkMatchingPasswords(password : TextView, rePassword : TextView) : Boolean{
        val validPw = checkValidInput(password)
        val validPwRe = checkValidInput(rePassword)
        if (validPw && validPwRe) {
            if (!password.text.toString().contentEquals(rePassword.text.toString())) {
                setModeOnTextView(password, "Password not identical", false)
                setModeOnTextView(rePassword, "Password not identical", false)
                return false
            }
            else {
                return true
            }
        }
        else
            return false
    }
}