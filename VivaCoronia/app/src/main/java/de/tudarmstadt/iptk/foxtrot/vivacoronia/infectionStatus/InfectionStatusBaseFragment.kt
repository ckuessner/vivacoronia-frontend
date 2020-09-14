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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ItemInfectionStatusDataBinding
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
        viewModel.newStatus.observe(viewLifecycleOwner, Observer { activity?.runOnUiThread { setInfectionState() } })
        setInfectionState()
        setIcons()

        return binding.root
    }

    private fun setInfectionState() {
        val infectionStatus = when (viewModel.newStatus.value) {
            "infected" -> InfectionStateLinearLayout.InfectionState.INFECTED
            "recovered" -> InfectionStateLinearLayout.InfectionState.RECOVERED
            viewModel.unknown -> InfectionStateLinearLayout.InfectionState.UNKNOWN
            else -> InfectionStateLinearLayout.InfectionState.UNKNOWN
        }
        binding.essentialInformation.infectionState = infectionStatus
    }

    private fun setIcons() {
        binding.infectionStatus.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_infection_status)
        binding.infectionDateApprox.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_estimated_date)
        binding.testDate.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_test_date)
    }

    private fun setAdditionalFields(additionalAttributes: JSONObject) {
        val additionalInfoLayout: LinearLayout = binding.additionalInformation
        additionalInfoLayout.removeAllViews()

        for (label in additionalAttributes.keys()) {
            val value = additionalAttributes.getString(label)
            val binding = DataBindingUtil.inflate<ItemInfectionStatusDataBinding>(layoutInflater, R.layout.item_infection_status_data, additionalInfoLayout, false)
            binding.label = label
            binding.value = value
            binding.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more)
            additionalInfoLayout.addView(binding.root)
        }
    }

    /**
     * Converts dp to px. scale needs to be set.
     */
    private fun px(dp: Int): Int {
        return (dp * scale).toInt()
    }
}
