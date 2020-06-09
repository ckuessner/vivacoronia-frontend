package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanQrCodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mScannerView = ZXingScannerView(this) // Programmatically initialize the scanner view
        setContentView(mScannerView) // Set the scanner view as the content view
    }

    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera() // Start camera on resume
    }

    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        if (rawResult.barcodeFormat != BarcodeFormat.QR_CODE) {
            mScannerView!!.resumeCameraPreview(this)
            return
        }

        // Setup view
        mScannerView!!.stopCamera()

        val mapper = jacksonObjectMapper()
        val data = mapper.readValue<HashMap<String, String>>(rawResult.text)
        val intent: Intent = Intent(this, UpdateInfectionActivity::class.java).apply {
            putExtra("data", data)
        }

        startActivity(intent)
    }
}
