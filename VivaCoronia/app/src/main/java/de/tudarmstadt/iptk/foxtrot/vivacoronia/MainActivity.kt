package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import de.tudarmstadt.iptk.foxtrot.locationPoster.LocationServerCommunicator
//import de.tudarmstadt.iptk.foxtrot.locationPoster.LocationService


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentContext : Context = this
        //val service = LocationService(currentContext)

        // TODO: Get userID
        var userID:Int = 12345

        val sendLocBtn = findViewById<Button>(R.id.sendLocationBtn)
        sendLocBtn.setOnClickListener {

            Toast.makeText(this, "Sending your position...", Toast.LENGTH_LONG).show()
            service.getSingleLocation()
        }
    }
}
