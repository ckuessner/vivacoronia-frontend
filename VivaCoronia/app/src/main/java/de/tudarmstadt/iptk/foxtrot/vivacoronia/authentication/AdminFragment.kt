package de.tudarmstadt.iptk.foxtrot.vivacoronia.authentication

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_admin, container, false)
        setLoginLogic(requireActivity(), view)
        return view
    }

    private fun setLoginLogic(ctx: Context, view: View){
        val loginBtn = view.findViewById<Button>(R.id.adminLogin)
        val firstPw = view.findViewById<TextView>(R.id.adminPw)
        val secondPw = view.findViewById<TextView>(R.id.adminPwRe)

        loginBtn.setOnClickListener {
            val canContinue = TextViewUtils.checkMatchingPasswords(firstPw, secondPw)
            if (canContinue){
                val pw = firstPw.text.toString()
                // we use empty userID because adminJWT doesn't need userID
                GlobalScope.launch {
                    val succJWT = AuthenticationCommunicator.makeNewJWT(ctx, pw, "", true)
                    requireActivity().runOnUiThread {
                        if(succJWT) //TODO: equivalent of fragment closing to get to mainActivity
                        else Toast.makeText(ctx, "Something went wrong, please check your internet and try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}