package de.tudarmstadt.iptk.foxtrot.vivacoronia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.locationPoster.locationServerCommunicator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sendLocBtn = findViewById<Button>(R.id.sendLocationBtn)
        sendLocBtn.setOnClickListener {
            Toast.makeText(this, "Sending your position...", Toast.LENGTH_LONG).show()
            locationServerCommunicator.sendCurrentPositionToServer()
        }
    }
}
