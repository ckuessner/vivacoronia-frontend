package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.collections.HashMap
import kotlin.concurrent.thread

const val UPLOAD_FAILED = 0
const val UPLOAD_SUCCESSFUL = 1
const val UPLOAD_IN_PROGRESS = 2
const val UPLOAD_DONE = 3

class UpdateInfectionActivity : AppCompatActivity() {
    private var currentUploadStatus = UPLOAD_DONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_infection)

        // Display data
        @Suppress("UNCHECKED_CAST")
        val data: HashMap<String, String> = intent.getSerializableExtra("data") as HashMap<String, String>
        initializeFields(data)
        setUploadStatus(UPLOAD_DONE)

        // Display loading screen

        // Send data to server and wait for response

        // Display response
    }

    private fun initializeFields(data: Map<String, String>) {
        val button: Button = findViewById(R.id.upload_infection_status)
        button.setOnClickListener { v -> uploadData(data) }

        setDefaultFields(data)

        val defaultAttributes = listOf("newStatus", "dateOfTest", "occuredDateEstimation", "signature")
        val additionalAttributes = data.filter { entry ->
            !defaultAttributes.contains(entry.key)
        }

        addAdditionalFields(additionalAttributes)
    }

    private fun uploadData(data: Map<String, String>) {
        // show loading Screen
        setUploadStatus(UPLOAD_IN_PROGRESS)
        val queue = Volley.newRequestQueue(this)
        val userId = "42"
        val apiBaseUrl = "http://192.168.2.176:3000" // TODO
        val url = "$apiBaseUrl/infection/$userId"
        val requestBody = JSONObject(data).toString()
        val request = object : StringRequest(Method.POST, url,
            Response.Listener { _ ->
                setUploadStatus(UPLOAD_SUCCESSFUL)
                resetUploadStatus(3000)
                // TODO Say thank you
            },
            Response.ErrorListener { _ ->
                // TODO Say try again / server not reachable
                setUploadStatus(UPLOAD_FAILED)
                resetUploadStatus(3000)
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        queue.add(request)
    }

    private fun resetUploadStatus(delayInMillis: Long){
        thread {
            Thread.sleep(delayInMillis)
            synchronized(currentUploadStatus) {
                if (currentUploadStatus != UPLOAD_IN_PROGRESS)
                    setUploadStatus(UPLOAD_DONE)
            }
        }
    }

    private fun setUploadStatus(newStatus: Int) {
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

        val viewToShow: View?
        when (newStatus) {
            UPLOAD_FAILED -> viewToShow = failedIcon
            UPLOAD_SUCCESSFUL -> viewToShow = successIcon
            UPLOAD_IN_PROGRESS -> {
                viewToShow = progressBar
                uploadButton.isEnabled = false
            }
            else -> viewToShow = null
        }

        if (viewToShow != null) {
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            viewToShow.startAnimation(fadeIn)
            viewToShow.visibility = View.VISIBLE
        }

        currentUploadStatus = newStatus
    }

    private fun addAdditionalFields(additionalAttributes: Map<String, String>) {
        val tableLayout: TableLayout = findViewById(R.id.update_infection_table)
        for ((key, value) in additionalAttributes) {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.weightSum = 1f

            val label = TextView(this)
            label.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            label.setPadding(3, 3, 3, 3)
            label.text = key
            row.addView(label)

            val data = TextView(this)
            //data.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, 0.5f)
            data.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            data.gravity = Gravity.END
            data.setPadding(3, 3, 3, 3)
            data.text = value
            row.addView(data)

            tableLayout.addView(row, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun setDefaultFields(data: Map<String, String>) {
        val infectionStatus: TextView = findViewById(R.id.infection_status)
        infectionStatus.text = data["newStatus"]

        val infectionDateApprox: TextView = findViewById(R.id.infection_date_approx)
        infectionDateApprox.text = data["occuredDateEstimation"]

        val testDate: TextView = findViewById(R.id.test_date)
        testDate.text = data["dateOfTest"]
    }
}
