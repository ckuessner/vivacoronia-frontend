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
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.InfectionApiClient
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
        try {
            return InfectionApiClient.getInfectionStatus(this)
        } catch (exception: ExecutionException) {
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
