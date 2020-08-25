package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AuthenticationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.floor


class StatusCheckFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_status, container, false)
        setStatusButtonLogic(requireActivity(), view)
        setUserIDShowLogic(requireActivity(), view)
        showExpiry(requireActivity(), view)
        return view
    }

    override fun onResume() {
        val isAdmin = requireActivity().getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getBoolean(Constants.IS_ADMIN, false)
        val adminJwt = requireActivity().getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.adminJWT, null)
        if(isAdmin && adminJwt != null) {
            view?.findViewById<TextView>(R.id.userStatus)?.text = "Feature permissions: Admin"
            showExpiry(requireActivity(), view as View)
        }
        super.onResume()
    }


    private fun setStatusButtonLogic(ctx: Context, view: View){
        //we're after register, so we can safely convert to String
        val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
        val statusCheckBtn = view.findViewById<Button>(R.id.statusChecker)
        statusCheckBtn.setOnClickListener {
            GlobalScope.launch {
                val succJWT = AuthenticationApiClient.checkStatus(ctx, userID)
                requireActivity().runOnUiThread {
                    when (succJWT) {
                        0 -> processCurrentStatus(ctx, view)
                        else -> RequestUtility.handleErrorShowing(ctx, succJWT)
                        }
                }
            }
        }
    }

    private fun setUserIDShowLogic(ctx: Context, view: View){
        val userIDShower = view.findViewById<Button>(R.id.showUserID)
        userIDShower.setOnClickListener{
            var userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null)
            requireActivity().runOnUiThread {
                Toast.makeText(
                    ctx,
                    "Your userID is: $userID, it was also copied to your clipboard",
                    Toast.LENGTH_LONG
                ).show()
            }
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", userID)
            clipboard.setPrimaryClip(clip)
        }
    }

    private fun showExpiry(ctx: Context, view: View){
        val isAdmin = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getBoolean(Constants.IS_ADMIN, false)
        val adminJwt = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.adminJWT, null)
        //to check whether we actually show something, check whether isAdmin
        if(!isAdmin || adminJwt == null){
            view.findViewById<TextView>(R.id.expiryInfo).visibility = View.GONE
            view.findViewById<TextView>(R.id.expiryTime).visibility = View.GONE
        }
        else {
            view.findViewById<TextView>(R.id.userStatus).text = "Feature permissions: Admin"
            view.findViewById<TextView>(R.id.expiryInfo).visibility = View.VISIBLE
            val expiry = view.findViewById<TextView>(R.id.expiryTime)
            expiry.visibility = View.VISIBLE

            //set time to show in expiry, init is 1 day
            val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
            val creationTime = settings.getLong(Constants.adminJWT_Time, 0)
            val currentTime = Calendar.getInstance().time.time
            val difference = currentTime - creationTime
            //convert remaining time to hours, minutes and seconds
            val toShowHour : Int = floor((difference / 3600).toDouble()).toInt()
            var remainingTime : Long = difference - toShowHour.toLong() * 3600
            val toShowMinute : Int = floor((remainingTime / 3600).toDouble()).toInt()
            remainingTime = difference - toShowMinute.toLong() * 60
            val toShowSeconds : Int = floor((remainingTime/60).toDouble()).toInt()
            expiry.text = "$toShowHour:$toShowMinute:$toShowSeconds"
        }
    }

    private fun processCurrentStatus(ctx: Context, view: View){
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        val currStatus = settings.getBoolean(Constants.IS_ADMIN, false)
        val noAdminJWT = settings.getString(Constants.adminJWT, null) == null
        if(currStatus && noAdminJWT){
            //give user possibility to login as admin, after we have received news that he can get adminJWT
            val builder = AlertDialog.Builder(ctx, R.style.AlertDialogTheme)
            builder.setTitle("Permission update")
            builder.setMessage("You can login as an admin to access admin features, do you want to do that?")
            //if user wants to use admin, he has to login for admin features
            builder.setPositiveButton(android.R.string.yes){ _, _ ->
                val intent = Intent(ctx, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("isAdmin", true)
                ctx.startActivity(intent)

            }
            builder.setNegativeButton(android.R.string.no){_, _ ->
                Toast.makeText(ctx, "If you wanna upgrade your access, reload status again and then click OK", Toast.LENGTH_SHORT).show()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        else {
            Toast.makeText(ctx, "Your permissions didn't change", Toast.LENGTH_SHORT).show()
        }
    }
}