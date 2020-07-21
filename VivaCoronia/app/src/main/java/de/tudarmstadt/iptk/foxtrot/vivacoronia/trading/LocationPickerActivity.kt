package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

private const val ARG_INITIAL_POSITION = "initial_position"
private const val SELECTED_POSITION_RESULT = "selected_position"

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var currentSelection: LatLng
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSelection = intent.getParcelableExtra(ARG_INITIAL_POSITION)!!
        setContentView(R.layout.activity_location_picker)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val confirmButton = findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener { finishAndReturnSelection() }
    }

    private fun finishAndReturnSelection() {
        val intent = Intent()
        intent.putExtra(SELECTED_POSITION_RESULT, currentSelection)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener {
            currentSelection = it
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it).title("Current selection"))
        }

        mMap.addMarker(MarkerOptions().position(currentSelection).title("Your position"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentSelection, 15f))
    }

    companion object {
        @JvmStatic
        fun getStartIntent(activity: Activity, modelPosition: LatLng): Intent {
            val initialPosition = getInitialPosition(activity, modelPosition)
            val intent = Intent(activity, LocationPickerActivity::class.java)
            intent.putExtra(ARG_INITIAL_POSITION, initialPosition)
            return intent
        }

        private fun getInitialPosition(activity: Activity, modelPosition: LatLng): LatLng {
            if (modelPosition != LatLng(0.0, 0.0))
                return modelPosition

            if (PermissionHandler.checkLocationPermissions(activity)) {
                val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                @SuppressLint("MissingPermission") // Check is in PermissionHandler
                val currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (currentLocation != null)
                    return LatLng(currentLocation.latitude, currentLocation.longitude)
            }

            return LatLng(49.877405, 8.654213)
        }

        fun getLatLngResult(intent: Intent): LatLng? {
            return intent.getParcelableExtra(SELECTED_POSITION_RESULT)
        }
    }
}