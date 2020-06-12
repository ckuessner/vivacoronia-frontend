package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.util.concurrent.ExecutionException
import kotlin.concurrent.thread

private const val TAG = "InfectionStatusActivity"
private const val ZXING_CAMERA_PERMISSION = 1


class InfectionStatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infection_status)

        loadCurrentInfectionStatus()

        val updateInfectionFab: View = findViewById(R.id.update_infection_fab)
        updateInfectionFab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, Array(1) { Manifest.permission.CAMERA },
                    ZXING_CAMERA_PERMISSION
                )
            else {
                val intent = Intent(this, ScanQrCodeActivity::class.java).apply {}
                startActivity(intent)
            }
        }
    }

    private fun loadCurrentInfectionStatus() {
        thread {
            val data = fetchData()
            if (!data.isNullOrEmpty())
                runOnUiThread {
                    InfectionStatusFragment.replaceFragment(
                        data,
                        supportFragmentManager
                    )
                }
        }
    }

    private fun fetchData(): HashMap<String, String> {
        val userId = Constants().USER_ID
        val apiBaseUrl = Constants().SERVER_BASE_URL
        val url = "$apiBaseUrl/infection/$userId"

        val queue = Volley.newRequestQueue(this)
        val future = RequestFuture.newFuture<String>()
        val request = StringRequest(Request.Method.GET, url, future, future)

        queue.add(request)
        try {
            val mapper = ObjectMapper()
            val result = future.get()
            return if (result != "") mapper.readValue(result) else HashMap()
        } catch (exception: ExecutionException){
            if (exception.cause is VolleyError && hasWindowFocus())
                runOnUiThread {
                    Toast.makeText(this, R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }
            else {
                Log.e(TAG, "Error while fetching or parsing current infection status", exception)
            }
        }
        return HashMap()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ZXING_CAMERA_PERMISSION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(this, ScanQrCodeActivity::class.java).apply {}
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
