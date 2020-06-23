package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.InfectionStatusBaseFragmentBinding
import org.json.JSONObject

class InfectionStatusBaseFragment : Fragment() {
    private lateinit var binding: InfectionStatusBaseFragmentBinding
    private lateinit var viewModel: InfectionStatusViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.infection_status_base_fragment,
            container,
            false
        )

        viewModel = ViewModelProvider(parentFragment?: requireActivity()).get(InfectionStatusViewModel::class.java)
        viewModel.additionalInfo.observe(viewLifecycleOwner, Observer {newValue -> setAdditionalFields(newValue)})
        binding.lifecycleOwner = viewLifecycleOwner
        binding.infectionStatusData = viewModel

        return binding.root
    }

    private fun setAdditionalFields(additionalAttributes: JSONObject) {
        val tableLayout: TableLayout = binding.updateInfectionTableAdditional
        tableLayout.removeAllViews()

        val padding: Int = (3 * resources.displayMetrics.density).toInt()
        for (key in additionalAttributes.keys()) {
            val attributeValue = additionalAttributes.getString(key)
            val separatorView = View(requireActivity())
            val scale = requireContext().resources.displayMetrics.density
            val widthPx = (1 * scale + 0.5f).toInt()
            separatorView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPx)
            separatorView.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.separatorColor))
            tableLayout.addView(separatorView)

            val row = TableRow(requireActivity())
            row.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.weightSum = 1f

            val labelView = TextView(requireActivity())
            labelView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            labelView.setPadding(padding, padding, padding, padding)
            labelView.text = key
            row.addView(labelView)

            val valueView = TextView(requireActivity())
            valueView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            valueView.gravity = Gravity.END
            labelView.setPadding(padding, padding, padding, padding)
            valueView.text = attributeValue
            row.addView(valueView)

            tableLayout.addView(row, TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT))
        }
    }
}


