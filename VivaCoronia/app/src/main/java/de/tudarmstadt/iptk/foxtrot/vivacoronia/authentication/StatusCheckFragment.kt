package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.AuthenticationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.RequestUtility
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
        showExpiry(requireActivity(), view as View)
        super.onResume()
    }


    private fun setStatusButtonLogic(ctx: Context, view: View){
        //we're after register, so we can safely convert to String
        val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null) as String
        val statusCheckBtn = view.findViewById<Button>(R.id.statusChecker)
        statusCheckBtn.setOnClickListener {
            val oldIsAdmin = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getBoolean(Constants.IS_ADMIN, false)
            GlobalScope.launch {
                requireActivity().runOnUiThread {
                    view.findViewById<ImageView>(R.id.iconInfo).visibility = View.GONE
                    view.findViewById<ProgressBar>(R.id.statusProgress).visibility = View.VISIBLE
                }
                val succJWT = AuthenticationApiClient.checkStatus(ctx, userID)
                requireActivity().runOnUiThread {
                    view.findViewById<ProgressBar>(R.id.statusProgress).visibility = View.GONE
                    view.findViewById<ImageView>(R.id.iconInfo).visibility = View.VISIBLE
                    when (succJWT) {
                        0 ->  checkStatus(ctx, view, oldIsAdmin)
                        else -> RequestUtility.handleErrorShowing(ctx, succJWT)
                    }
                }
            }
        }
    }

    private fun setUserIDShowLogic(ctx: Context, view: View){
        val userIDShower = view.findViewById<Button>(R.id.showUserID)
        userIDShower.setOnClickListener{
            val userID = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).getString(Constants.USER_ID, null)
            requireActivity().runOnUiThread {
                Toast.makeText(
                    ctx,
                    "Your UserID is: $userID, it was also copied to your clipboard",
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
        if(!isAdmin || adminJwt == null) {
            val imageView = view.findViewById<ImageView>(R.id.iconInfo)
            imageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_user))
            view.findViewById<TextView>(R.id.userStatus).text =
                getString(R.string.feature_permissions_user)
            (activity as MainActivity?)?.hideAdminFeatures()
            (activity as MainActivity?)?.changeStatusIcon(false)
        }
        else {
            val imageView = view.findViewById<ImageView>(R.id.iconInfo)
            imageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_admin))
            view.findViewById<TextView>(R.id.userStatus).text =
                getString(R.string.feature_permission_admin)
        }
    }

    private fun checkStatus(ctx: Context, view: View, oldIsAdmin : Boolean){
        val settings = ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        val currStatus = settings.getBoolean(Constants.IS_ADMIN, false)
        val noAdminJWT = settings.getString(Constants.adminJWT, null) == null
        //if our status changed and we're now admin, we wanna show permission update
        if((currStatus && oldIsAdmin != currStatus) || (currStatus && noAdminJWT)){
            //give user possibility to login as admin, after we have received news that he can get adminJWT
            val builder = AlertDialog.Builder(ctx, R.style.AlertDialogTheme)
            builder.setTitle(getString(R.string.permissionUpdate))
            builder.setMessage(getString(R.string.permissionUpdateAlert))
            //if user wants to use admin, he has to login for admin features
            builder.setPositiveButton(android.R.string.yes){ _, _ ->
                val intent = Intent(ctx, LoginActivity::class.java)
                intent.putExtra("isAdmin", true)
                ctx.startActivity(intent)

            }
            builder.setNegativeButton(android.R.string.no){_, _ ->
                Toast.makeText(ctx, getString(R.string.remindPermissionUpdate), Toast.LENGTH_SHORT).show()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
        //if our status changed to no admin, but we had an adminJWT and were admin before change fragment to userFragment
        else if(!currStatus && !noAdminJWT && oldIsAdmin != currStatus){
            (activity as MainActivity?)?.changeStatusIcon(currStatus)
            //change icon back to user icon
            val imageView = view.findViewById<ImageView>(R.id.iconInfo)
            imageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_user))

            view.findViewById<TextView>(R.id.userStatus).text = getString(R.string.feature_permissions_user)
            //set adminJWT to zero
            ctx.getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE).edit().putString(Constants.adminJWT, null).apply()
            (activity as MainActivity?)?.hideAdminFeatures()
        }
        else{
            Toast.makeText(ctx, getString(R.string.samePermission), Toast.LENGTH_SHORT).show()
        }
    }
}