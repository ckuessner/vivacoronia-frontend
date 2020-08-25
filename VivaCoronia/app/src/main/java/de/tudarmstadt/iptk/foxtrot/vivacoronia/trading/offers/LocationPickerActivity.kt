package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.utils.LocationUtility

private const val ARG_INITIAL_POSITION = "initial_position"
private const val ARG_INITIAL_RADIUS = "initial_radius"
private const val ARG_USE_RADIUS = "use_radius"
private const val SELECTED_POSITION_RESULT = "selected_position"
private const val SELECTED_RADIUS_RESULT = "selected_radius"

class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback, SeekBar.OnSeekBarChangeListener {
    private lateinit var currentPosition: LatLng
    private var currentRadius: Int = -1
    private var useRadius: Boolean = false
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPosition = intent.getParcelableExtra(ARG_INITIAL_POSITION)!!
        useRadius = intent.getBooleanExtra(ARG_USE_RADIUS, false)
        setContentView(R.layout.activity_location_picker)

        if (useRadius) {
            currentRadius = intent.getIntExtra(ARG_INITIAL_RADIUS, -1)
            val seekBarContainer = findViewById<View>(R.id.seekBar_container)
            seekBarContainer.visibility = View.VISIBLE

            val distanceTextView = findViewById<TextView>(R.id.distanceText)
            distanceTextView.text = getString(R.string.searchRadius, currentRadius.toString()) // initializes if currentRadius == 0
            val seekBar = findViewById<SeekBar>(R.id.seekbar)
            seekBar.setOnSeekBarChangeListener(this)
            seekBar.progress = currentRadius
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val confirmButton = findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener { finishAndReturnSelection() }
    }

    private fun finishAndReturnSelection() {
        val intent = Intent()
        intent.putExtra(SELECTED_POSITION_RESULT, currentPosition)
        intent.putExtra(SELECTED_RADIUS_RESULT, currentRadius)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapClickListener {
            currentPosition = it
            drawCurrentState()
        }

        drawCurrentState()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15f))
    }

    private fun drawCurrentState() {
        if (!this::mMap.isInitialized)
            return
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(currentPosition).title("Current selection"))
        if (useRadius) {
            val circleOptions = CircleOptions()
                .center(currentPosition)
                .radius(currentRadius.toDouble() * 1000)
                .fillColor(Color.parseColor("#447aff85"))
                .strokeColor(Color.BLACK)
                .strokeWidth(3F)
            mMap.addCircle(circleOptions)
        }
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val distanceTextView = findViewById<TextView>(R.id.distanceText)
        distanceTextView.text = getString(R.string.searchRadius, progress.toString())

        //logic to calculate text position to follow seekBar slider
        val width = seekBar!!.width - seekBar.paddingLeft - seekBar.paddingRight
        var thumbPos = width * (seekBar.progress / (seekBar.max.toFloat())) - 35
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        if((thumbPos + distanceTextView.width + 20) > screenWidth && thumbPos > distanceTextView.x){
            thumbPos = distanceTextView.x
        }
        if(thumbPos - 20 < 0 && thumbPos < distanceTextView.x){
            thumbPos = distanceTextView.x
        }
        distanceTextView.x = thumbPos

        currentRadius = seekBar.progress
        drawCurrentState()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        currentRadius = seekBar!!.progress
        drawCurrentState()
    }

    companion object {
        @JvmStatic
        fun getStartIntent(activity: Activity, modelPosition: LatLng, modelRadiusInKm: Int?): Intent {
            val initialPosition = getInitialPosition(activity, modelPosition)
            val intent = Intent(activity, LocationPickerActivity::class.java)
            intent.putExtra(ARG_INITIAL_POSITION, initialPosition)
            intent.putExtra(ARG_INITIAL_RADIUS, modelRadiusInKm)
            intent.putExtra(ARG_USE_RADIUS, modelRadiusInKm != null && modelRadiusInKm >= 0)
            return intent
        }

        private fun getInitialPosition(activity: Activity, modelPosition: LatLng): LatLng {
            if (modelPosition != LatLng(0.0, 0.0))
                return modelPosition

            return LocationUtility.getLastKnownLocation(activity) ?: LatLng(49.877405, 8.654213)
        }

        fun getLatLngResult(intent: Intent): LatLng? {
            return intent.getParcelableExtra(SELECTED_POSITION_RESULT)
        }

        fun getRadiusResult(intent: Intent): Int {
            return intent.getIntExtra(SELECTED_RADIUS_RESULT, -1)
        }
    }
}