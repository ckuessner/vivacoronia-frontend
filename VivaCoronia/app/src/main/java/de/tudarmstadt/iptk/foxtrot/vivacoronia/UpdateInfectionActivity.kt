package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
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
            Response.Listener { response ->
                setUploadStatus(UPLOAD_SUCCESSFUL)
                thread {
                    Thread.sleep(3000)
                    synchronized(currentUploadStatus) {
                        if (currentUploadStatus != UPLOAD_IN_PROGRESS)
                            setUploadStatus(UPLOAD_DONE)
                    }
                }
                // TODO Say thank you
                println(response)
            },
            Response.ErrorListener { error ->
                // TODO Say try again / server not reachable
                setUploadStatus(UPLOAD_FAILED)
                thread {
                    Thread.sleep(3000)
                    synchronized(currentUploadStatus) {
                        if (currentUploadStatus != UPLOAD_IN_PROGRESS)
                            setUploadStatus(UPLOAD_DONE)
                    }
                }

                println(error.message)
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

    private fun setUploadStatus(status: Int) {
        currentUploadStatus = status

        val progressBar: View = findViewById(R.id.upload_infection_status_progress_bar)
        val successIcon: View = findViewById(R.id.update_infection_status_success)
        val failedIcon: View = findViewById(R.id.update_infection_status_failed)
        val uploadButton: Button = findViewById(R.id.upload_infection_status)

        when (status) {
            UPLOAD_FAILED -> {
                progressBar.visibility = View.INVISIBLE
                successIcon.visibility = View.INVISIBLE
                failedIcon.visibility = View.VISIBLE
                uploadButton.isEnabled = true
            }
            UPLOAD_SUCCESSFUL -> {
                progressBar.visibility = View.INVISIBLE
                successIcon.visibility = View.VISIBLE
                failedIcon.visibility = View.INVISIBLE
                uploadButton.isEnabled = true
            }
            UPLOAD_IN_PROGRESS -> {
                progressBar.visibility = View.VISIBLE
                successIcon.visibility = View.INVISIBLE
                failedIcon.visibility = View.INVISIBLE
                uploadButton.isEnabled = false
            }
            else -> {
                progressBar.visibility = View.INVISIBLE
                successIcon.visibility = View.INVISIBLE
                failedIcon.visibility = View.INVISIBLE
                uploadButton.isEnabled = true
            }
        }

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
