package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.InfectionApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentInfectionStatusBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ExecutionException

private const val TAG = "InfectionStatusFragment"

class InfectionStatusFragment : Fragment() {
    private lateinit var binding: FragmentInfectionStatusBinding
    private lateinit var viewModel: InfectionStatusViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModelFactory = InfectionStatusViewModelFactory(resources.getString(R.string.unknown))
        viewModel = ViewModelProvider(this, viewModelFactory).get(InfectionStatusViewModel::class.java)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_infection_status,
            container,
            false
        )

        binding.updateInfectionFab.setOnClickListener {
            if (!PermissionHandler.checkCameraPermissions(requireActivity()))
                PermissionHandler.requestCameraPermissions(this)
            else {
                val intent = Intent(requireActivity(), ScanQrCodeActivity::class.java).apply {}
                startActivity(intent)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadCurrentInfectionStatus()
    }

    private fun loadCurrentInfectionStatus() {
        GlobalScope.launch {
            val data = fetchData()
            if (data.length() != 0) {
                requireActivity().runOnUiThread {
                    viewModel.update(data)
                }
            }
        }
    }

    private fun fetchData(): JSONObject {
        try {
            return InfectionApiClient.getInfectionStatus(requireActivity())
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && requireActivity().hasWindowFocus())
                if (((exception.cause as VolleyError).networkResponse?.statusCode ?: -1) != 404) // if 404, no user data found => no error
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireActivity(), R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                    }
            else {
                Log.e(TAG, "Error while fetching or parsing current infection status", exception)
            }
        }
        return JSONObject()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.CAMERA_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(requireActivity(), ScanQrCodeActivity::class.java).apply {}
                    startActivity(intent)
                } else {
                    Toast.makeText(requireActivity(), "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show()
                }
        }
    }
}