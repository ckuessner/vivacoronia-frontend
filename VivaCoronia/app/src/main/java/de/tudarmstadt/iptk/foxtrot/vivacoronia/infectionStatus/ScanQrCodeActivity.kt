package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanQrCodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    companion object {
        const val UPDATE_INFECTION_STATUS_ACTIVITY = 1
    }

    private var mScannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        if (rawResult.barcodeFormat != BarcodeFormat.QR_CODE) {
            mScannerView!!.resumeCameraPreview(this)
            return
        }

        mScannerView!!.stopCamera()

        val intent: Intent = Intent(this, UpdateInfectionActivity::class.java).apply {
            putExtra("data", rawResult.text)
        }

        startActivityForResult(intent, UPDATE_INFECTION_STATUS_ACTIVITY)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UPDATE_INFECTION_STATUS_ACTIVITY)
            finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
