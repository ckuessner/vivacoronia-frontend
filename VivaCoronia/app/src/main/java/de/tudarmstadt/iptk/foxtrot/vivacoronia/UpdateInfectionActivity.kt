package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.app.ActionBar
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.zxing.BarcodeFormat
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result
import java.util.*
import kotlin.collections.HashMap

class UpdateInfectionActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
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

        mScannerView!!.stopCamera()
        setContentView(R.layout.activity_update_infection)
        // Display data and wait for approval
        val mapper = jacksonObjectMapper()
        val jsonResult = mapper.readValue<HashMap<String, String>>(rawResult.text)

        setDefaultFields(jsonResult)

        val defaultAttributes = listOf("newStatus", "dateOfTest", "occuredDateEstimation", "signature")
        val additionalAttributes = jsonResult.filter { entry ->
            !defaultAttributes.contains(entry.key)
        }
        addAdditionalFields(additionalAttributes)
        // Display loading screen

        // Send data to server and wait for response

        // Display response
    }

    private fun addAdditionalFields(additionalAttributes: Map<String, String>) {
        val tableLayout: TableLayout = findViewById(R.id.update_infection_table)
        for ((key, value) in additionalAttributes) {
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.weightSum = 1f

            val label = TextView(this)
            label.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            label.setPadding(3, 3, 3, 3)
            label.text = key
            row.addView(label)

            val data = TextView(this)
            //data.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 0, 0.5f)
            data.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            data.gravity = Gravity.END
            data.setPadding(3, 3, 3, 3)
            data.text = value
            row.addView(data)

            tableLayout.addView(row, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun setDefaultFields(jsonResult: HashMap<String, String>) {
        val infectionStatus: TextView = findViewById(R.id.infection_status)
        infectionStatus.text = jsonResult["newStatus"]

        val infectionDateApprox: TextView = findViewById(R.id.infection_date_approx)
        infectionDateApprox.text = jsonResult["occuredDateEstimation"]

        val testDate: TextView = findViewById(R.id.test_date)
        testDate.text = jsonResult["dateOfTest"]
    }
}
