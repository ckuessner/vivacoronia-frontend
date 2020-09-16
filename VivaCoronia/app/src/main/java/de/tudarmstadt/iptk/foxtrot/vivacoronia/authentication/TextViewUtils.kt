package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.util.Patterns
import android.widget.TextView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

object TextViewUtils {

    /*
    this only checks whether input isn't empty
     */
    fun checkValidInput(textView: TextView):Boolean {
        var mode = true
        if (textView.text.isEmpty()) {
            mode = false
        }

        setTextViewBackground(textView, "Invalid Input", mode)

        return mode
    }

    fun checkValidInput(txt: String, isEmail: Boolean) : Boolean{
        var mode = true
        if(txt.isEmpty())
            mode = false
        if(isEmail && mode){
            val pattern = Patterns.EMAIL_ADDRESS
            mode = pattern.matcher(txt).matches()
        }
        return mode
    }

    private fun setTextViewBackground(textView: TextView, msg:String, isValidInput:Boolean) {
        if (isValidInput) {
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
                setTextViewBackground(password, "Password not identical", false)
                setTextViewBackground(rePassword, "Password not identical", false)
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