package de.tudarmstadt.iptk.foxtrot.vivacoronia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.concurrent.thread

private const val ZXING_CAMERA_PERMISSION = 1

class InfectionStatusActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infection_status)

        loadCurrentInfectionStatus()

        val updateInfectionFab: View = findViewById(R.id.update_infection_fab)
        updateInfectionFab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, Array(1) { Manifest.permission.CAMERA }, ZXING_CAMERA_PERMISSION)
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
                    InfectionStatusFragment.replaceFragment(data, supportFragmentManager)
                }
        }
    }

    private fun fetchData(): HashMap<String, String> {
        val userId = "42"
        val apiBaseUrl = "http://192.168.2.176:3000" // TODO load from config
        val url = "$apiBaseUrl/infection/$userId"

        val queue = Volley.newRequestQueue(this)
        val future = RequestFuture.newFuture<String>()
        val request = StringRequest(Request.Method.GET, url, future, future)

        queue.add(request)
        try {
            val mapper = ObjectMapper()
            return mapper.readValue(future.get())
        } catch (e: Exception) {
            // TODO exception abfangen und was anzeigen
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
