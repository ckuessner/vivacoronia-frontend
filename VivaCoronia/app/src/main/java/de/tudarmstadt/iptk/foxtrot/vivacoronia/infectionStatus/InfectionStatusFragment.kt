package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.text.SimpleDateFormat
import java.util.Locale


class InfectionStatusFragment : Fragment() {
    companion object {
        fun replaceFragment(data: HashMap<String, String>, fragmentManager: FragmentManager) {
            val bundle = Bundle()
            bundle.putSerializable("data", data)
            val fragment = InfectionStatusFragment()
            fragment.arguments = bundle
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.infection_status_fragment, fragment)
            transaction.commit()
        }
    }

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) // TODO zeigt noch die falsche Uhrzeit an (zeigt UTC statt local an)
    private val formatter: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.infection_status_fragment, container, false)

        if (arguments != null && arguments!!.containsKey("data")) {
            @Suppress("UNCHECKED_CAST") // TODO Check type and Log wrong type if inappropriate
            val data: HashMap<String, String> = arguments!!.get("data") as HashMap<String, String>
            setFields(data, view)
        }

        return view
    }

    private fun setFields(data: HashMap<String, String>, view: View) {
        setDefaultFields(data, view)

        val defaultAttributes = listOf("newStatus", "dateOfTest", "occuredDateEstimation", "signature")
        val additionalAttributes = data.filter { entry ->
            !defaultAttributes.contains(entry.key)
        }

        addAdditionalFields(additionalAttributes, view)
    }

    private fun addAdditionalFields(additionalAttributes: Map<String, String>, view: View) {
        val tableLayout: TableLayout = view.findViewById(R.id.update_infection_table)
        val padding: Int = (3 * resources.displayMetrics.density).toInt()
        for ((key, attributeValue) in additionalAttributes) {
            val row = TableRow(activity!!)
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.weightSum = 1f

            val labelView = TextView(activity!!)
            labelView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            labelView.setPadding(padding, padding, padding, padding)
            labelView.text = key
            row.addView(labelView)

            val valueView = TextView(activity!!)
            valueView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            valueView.gravity = Gravity.END
            labelView.setPadding(padding, padding, padding, padding)
            valueView.text = attributeValue
            row.addView(valueView)

            tableLayout.addView(row, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun setDefaultFields(data: HashMap<String, String>, view: View) {
        val infectionStatus: TextView = view.findViewById(R.id.infection_status)
        infectionStatus.text = data["newStatus"]

        val infectionDateApprox: TextView = view.findViewById(R.id.infection_date_approx)
        infectionDateApprox.text = formatter.format(parser.parse(data["occuredDateEstimation"]))

        val testDate: TextView = view.findViewById(R.id.test_date)
        testDate.text = formatter.format(parser.parse(data["dateOfTest"]))
    }
}
