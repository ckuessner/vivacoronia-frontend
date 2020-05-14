package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    // private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // check wheter location permission was granted
        // get permissions
        val coarseLocationPermitted = ActivityCompat
            .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val fineLocationPermitted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        // if android version bigger equal 10 then also background location access has to be permitted
        // TODO check wheter background location access is needed because it is needed for API 29 and higher
        // check permission
        if (coarseLocationPermitted && fineLocationPermitted) {
            // start location service
            requestLocationService()
        }
        else {
            // request location permissions
            requestLocationPermissions()
        }
    }

    fun requestLocationService() {
        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    /**
     * handles permission requests
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            // handle location permission requests
            LOCATION_ACCESS_PERMISSION_REQUEST_CODE -> {
                // request permissions again if location permission not granted
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                    requestLocationPermissions()
                }
                else {
                    requestLocationService()
                }
            }
        }
    }

    /**
     * requests permissions for location services
     */
    fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_ACCESS_PERMISSION_REQUEST_CODE
        )
    }
}
