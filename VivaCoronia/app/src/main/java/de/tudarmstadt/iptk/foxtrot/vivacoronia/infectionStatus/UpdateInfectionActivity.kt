package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.android.volley.ClientError
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.InfectionApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ActivityUpdateInfectionBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.mainActivity.MainActivity
import org.json.JSONObject
import kotlin.concurrent.thread

private const val UPLOAD_FAILED = 0
private const val UPLOAD_SUCCESSFUL = 1
private const val UPLOAD_IN_PROGRESS = 2
private const val NO_UPLOAD_STATUS = 3

class UpdateInfectionActivity : AppCompatActivity() {
    private var currentUploadStatus = NO_UPLOAD_STATUS
    private lateinit var binding: ActivityUpdateInfectionBinding
    private lateinit var viewModel: InfectionStatusViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModelFactory = InfectionStatusViewModelFactory(resources.getString(R.string.unknown))
        viewModel = ViewModelProvider(this, viewModelFactory).get(InfectionStatusViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_infection)

        // Display data
        val data = JSONObject(intent.getStringExtra("data")!!)
        viewModel.update(data)

        binding.uploadInfectionStatus.setOnClickListener {
            uploadData(data)
        }
    }

    private fun uploadData(data: JSONObject) {
        setUploadStatus(UPLOAD_IN_PROGRESS)
        InfectionApiClient.postInfectionStatus(this, data, ::onUploadSuccessful, ::onUploadFailed)
    }

    private fun onUploadFailed(e: VolleyError) {
        if (e is ClientError)
            Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
        setUploadStatus(UPLOAD_FAILED)
        resetUploadStatusDelayed()
    }

    private fun onUploadSuccessful() {
        setUploadStatus(UPLOAD_SUCCESSFUL)
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val handler = Handler()
        handler.postDelayed({
            if (hasWindowFocus()){
                val retIntent = Intent()
                setResult(Activity.RESULT_OK, retIntent)
                finish()
            }
                //startActivity(intent)
        }, 2000)
    }

    private fun resetUploadStatusDelayed() {
        val delayInMillis = 3000L
        thread {
            Thread.sleep(delayInMillis)
            synchronized(currentUploadStatus) {
                if (currentUploadStatus != UPLOAD_IN_PROGRESS)
                    setUploadStatus(NO_UPLOAD_STATUS)
            }
        }
    }

    private fun resetUploadStatus() {
        var viewToHide: View = binding.uploadInfectionStatusProgressBar
        when (currentUploadStatus) {
            UPLOAD_FAILED -> viewToHide = binding.updateInfectionStatusFailed
            UPLOAD_SUCCESSFUL -> viewToHide = binding.updateInfectionStatusSuccess
        }
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        viewToHide.startAnimation(fadeOut)
        viewToHide.visibility = View.INVISIBLE
        binding.uploadInfectionStatus.isEnabled = true
    }

    private fun setUploadStatus(newStatus: Int) {
        resetUploadStatus()
        currentUploadStatus = newStatus
        if (newStatus == NO_UPLOAD_STATUS)
            return

        val viewToShow: View?
        when (newStatus) {
            UPLOAD_FAILED -> viewToShow = binding.updateInfectionStatusFailed
            UPLOAD_SUCCESSFUL -> viewToShow = binding.updateInfectionStatusSuccess
            UPLOAD_IN_PROGRESS -> {
                viewToShow = binding.uploadInfectionStatusProgressBar
                binding.uploadInfectionStatus.isEnabled = false
            }
            else -> return
        }

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        viewToShow.startAnimation(fadeIn)
        viewToShow.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
