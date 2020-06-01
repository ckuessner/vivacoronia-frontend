package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationPoster.LocationServerCommunicator
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationPoster.LocationService


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init LocationService
        val currentContext : Context = this
        val service = LocationService(currentContext)


        // register listener for button to send current location
        val sendLocBtn = findViewById<Button>(R.id.sendLocationBtn)
        sendLocBtn.setOnClickListener {
            // Call location service which will send the location to the communicator
            service.sendSingleLocation()
        }
    }
}
