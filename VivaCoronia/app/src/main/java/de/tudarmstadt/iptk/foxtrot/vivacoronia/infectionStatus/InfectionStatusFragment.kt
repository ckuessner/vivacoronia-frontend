package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.InfectionApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentInfectionStatusBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread

private const val TAG = "InfectionStatusFragment"
private const val ZXING_CAMERA_PERMISSION = 1

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

        loadCurrentInfectionStatus()

        binding.updateInfectionFab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(requireActivity(), Array(1) { Manifest.permission.CAMERA },
                    ZXING_CAMERA_PERMISSION
                )
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
            ZXING_CAMERA_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(requireActivity(), ScanQrCodeActivity::class.java).apply {}
                    startActivity(intent)
                } else {
                    Toast.makeText(requireActivity(), "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show()
                }
        }
    }
}