package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
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
    private var scale: Float = 0f


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
        scale = requireContext().resources.displayMetrics.density

        viewModel = ViewModelProvider(
            parentFragment ?: requireActivity()
        ).get(InfectionStatusViewModel::class.java)
        viewModel.additionalInfo.observe(
            viewLifecycleOwner,
            Observer { newValue -> setAdditionalFields(newValue) })
        binding.lifecycleOwner = viewLifecycleOwner
        binding.infectionStatusData = viewModel
        viewModel.newStatus.observe(
            viewLifecycleOwner,
            Observer { activity?.runOnUiThread { setColor() } })
        setColor()

        return binding.root
    }

    private fun setColor() {
        val colorId = when (viewModel.newStatus.value) {
            "infected" -> R.color.red
            "recovered" -> R.color.green
            viewModel.unknown -> R.color.separatorColor
            else -> R.color.separatorColor
        }
        binding.textColor = ContextCompat.getColor(requireActivity(), colorId)
    }

    private fun setAdditionalFields(additionalAttributes: JSONObject) {
        val additionalInfoLayout: LinearLayout = binding.additionalInformation
        additionalInfoLayout.removeAllViews()

        val padding: Int = px(3)
        val separatorHeight = px(1)
        for (key in additionalAttributes.keys()) {
            val separatorView = View(requireActivity())
            val layoutParams =
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, separatorHeight)
            layoutParams.setMargins(px(10))
            separatorView.layoutParams = layoutParams
            separatorView.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.separatorColor
                )
            )

            val additionalItemLayout = RelativeLayout(requireActivity())
            val additionalItemLayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            additionalItemLayout.layoutParams = additionalItemLayoutParams

            val iconDummy = TextView(requireActivity())
            iconDummy.id = R.id.additional_icon
            val iconParams = RelativeLayout.LayoutParams(px(40), px(40))
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            iconDummy.layoutParams = iconParams

            val labelView = TextView(requireActivity())
            labelView.id = R.id.additional_label
            val labelParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            labelParams.addRule(RelativeLayout.END_OF, iconDummy.id)
            labelView.layoutParams = labelParams
            labelView.setPadding(padding)
            labelView.text = key
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)

            val attributeValue = additionalAttributes.getString(key)
            val valueView = TextView(requireActivity())
            val valueParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            valueParams.addRule(RelativeLayout.BELOW, labelView.id)
            valueParams.addRule(RelativeLayout.END_OF, iconDummy.id)
            valueView.layoutParams = valueParams
            valueView.setPadding(padding)
            valueView.text = attributeValue
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            valueView.setTextColor(binding.textColor)

            additionalItemLayout.addView(iconDummy)
            additionalItemLayout.addView(labelView)
            additionalItemLayout.addView(valueView)

            additionalInfoLayout.addView(separatorView)
            additionalInfoLayout.addView(additionalItemLayout)
        }
    }

    /**
     * Converts dp to px. scale needs to be set.
     */
    private fun px(dp: Int): Int {
        return (dp * scale).toInt()
    }
}
