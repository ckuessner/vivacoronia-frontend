package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.ClientError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import org.json.JSONObject
import kotlin.collections.HashMap
import kotlin.concurrent.thread

private const val UPLOAD_FAILED = 0
private const val UPLOAD_SUCCESSFUL = 1
private const val UPLOAD_IN_PROGRESS = 2
private const val NO_UPLOAD_STATUS = 3

class UpdateInfectionActivity : AppCompatActivity() {
    private var currentUploadStatus = NO_UPLOAD_STATUS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_infection)

        // Display data
        @Suppress("UNCHECKED_CAST")  // TODO Check type and Log "wrong type" if inappropriate
        val data = intent.getSerializableExtra("data") as HashMap<String, String>
        InfectionStatusFragment.replaceFragment(data, supportFragmentManager)

        val button: Button = findViewById(R.id.upload_infection_status)
        button.setOnClickListener { uploadData(data) }
    }

    private fun uploadData(data: Map<String, String>) {
        setUploadStatus(UPLOAD_IN_PROGRESS)
        val userId = Constants().USER_ID
        val apiBaseUrl = Constants().SERVER_BASE_URL
        val url = "$apiBaseUrl/infection/$userId"

        val requestBody = JSONObject(data).toString()
        val request = object : StringRequest(Method.POST, url,
            Response.Listener { onUploadSuccessful() },
            Response.ErrorListener { e -> onUploadFailed(e) }
        ) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun onUploadFailed(e: VolleyError) {
        if (e is ClientError)
            Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, R.string.server_connection_failed, Toast.LENGTH_SHORT).show()
        setUploadStatus(UPLOAD_FAILED)
        resetUploadStatusDelayed(3000)
    }

    private fun onUploadSuccessful() {
        setUploadStatus(UPLOAD_SUCCESSFUL)
        resetUploadStatusDelayed(3000)
        Thread.sleep(3000)
        val intent = Intent(this, InfectionStatusActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun resetUploadStatusDelayed(delayInMillis: Long) {
        thread {
            Thread.sleep(delayInMillis)
            synchronized(currentUploadStatus) {
                if (currentUploadStatus != UPLOAD_IN_PROGRESS)
                    setUploadStatus(NO_UPLOAD_STATUS)
            }
        }
    }

    private fun resetUploadStatus() {
        val progressBar: View = findViewById(R.id.upload_infection_status_progress_bar)
        val successIcon: View = findViewById(R.id.update_infection_status_success)
        val failedIcon: View = findViewById(R.id.update_infection_status_failed)
        val uploadButton: Button = findViewById(R.id.upload_infection_status)

        var viewToHide: View = progressBar
        when (currentUploadStatus) {
            UPLOAD_FAILED -> viewToHide = failedIcon
            UPLOAD_SUCCESSFUL -> viewToHide = successIcon
            UPLOAD_IN_PROGRESS -> viewToHide = progressBar
        }
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        viewToHide.startAnimation(fadeOut)
        viewToHide.visibility = View.INVISIBLE
        uploadButton.isEnabled = true
    }

    private fun setUploadStatus(newStatus: Int) {
        val progressBar: View = findViewById(R.id.upload_infection_status_progress_bar)
        val successIcon: View = findViewById(R.id.update_infection_status_success)
        val failedIcon: View = findViewById(R.id.update_infection_status_failed)
        val uploadButton: Button = findViewById(R.id.upload_infection_status)

        resetUploadStatus()
        currentUploadStatus = newStatus
        if (newStatus == NO_UPLOAD_STATUS)
            return

        val viewToShow: View?
        when (newStatus) {
            UPLOAD_FAILED -> viewToShow = failedIcon
            UPLOAD_SUCCESSFUL -> viewToShow = successIcon
            UPLOAD_IN_PROGRESS -> {
                viewToShow = progressBar
                uploadButton.isEnabled = false
            }
            else -> return
        }

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        viewToShow.startAnimation(fadeIn)
        viewToShow.visibility = View.VISIBLE
    }
}
