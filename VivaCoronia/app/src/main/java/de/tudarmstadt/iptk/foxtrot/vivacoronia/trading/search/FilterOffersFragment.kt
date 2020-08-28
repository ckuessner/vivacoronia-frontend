package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentFilterOffersBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.LocationPickerActivity

private const val LOCATION_PICKER_REQUEST = 1


class FilterOffersFragment(internal var callback: OnApplyQueryListener) : Fragment(), OnSeekBarChangeListener, AdapterView.OnItemSelectedListener,
    RadioGroup.OnCheckedChangeListener {
    private lateinit var viewModel: SearchOffersViewModel
    private lateinit var binding: FragmentFilterOffersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(SearchOffersViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter_offers, container, false)
        binding.searchOffers = viewModel
        binding.doneButton.setOnClickListener { onDoneButtonClick() }
        binding.locationPickerButton.setOnClickListener { onSelectLocation() }
        binding.radiusSeekbar.setOnSeekBarChangeListener(this)
        binding.radiusSeekbar.progress = viewModel.searchQuery.value!!.radiusInKm

        val categories = Offer.categories.value!!.toMutableList()
        categories.add(0, "Any category")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, categories)
        binding.categoryInputSpinner.adapter = spinnerAdapter
        binding.categoryInputSpinner.onItemSelectedListener = this
        binding.categoryInputSpinner.setSelection(spinnerAdapter.getPosition(viewModel.searchQuery.value!!.category))

        binding.sortByRadioGroup.setOnCheckedChangeListener(this)
        binding.sortByRadioGroup.check(mapSortOptionToId(viewModel.searchQuery.value!!.sortBy))
        return binding.root
    }

    private fun onDoneButtonClick() {
        parentFragmentManager.popBackStack()
        callback.onApplyQuery(viewModel.searchQuery.value!!)
    }

    private fun onSelectLocation() {
        val initialLocation = viewModel.searchQuery.value!!.location ?: LatLng(0.0, 0.0)
        val initialRadius = viewModel.searchQuery.value!!.radiusInKm
        val intent = LocationPickerActivity.getStartIntent(requireActivity(), initialLocation, initialRadius)
        startActivityForResult(intent, LOCATION_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val latLngResult = LocationPickerActivity.getLatLngResult(data) ?: return
            val radiusResult = LocationPickerActivity.getRadiusResult(data)
            viewModel.searchQuery.value!!.location = latLngResult
            viewModel.searchQuery.value!!.radiusInKm = radiusResult
            binding.radiusSeekbar.progress = radiusResult
        }
    }

    override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
        binding.radiusText.text = resources.getString(R.string.searchRadius).format(progress)
        viewModel.searchQuery.value!!.radiusInKm = progress
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0)
            viewModel.searchQuery.value!!.category = parent?.getItemAtPosition(position) as String
        else
            viewModel.searchQuery.value!!.category = ""
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        viewModel.searchQuery.value!!.sortBy = mapIdToSortOption(checkedId)
    }

    private fun mapIdToSortOption(id: Int): ProductSearchQuery.SortOptions {
        return when (id) {
            R.id.name_radio_button -> ProductSearchQuery.SortOptions.NAME
            R.id.distance_radio_button -> ProductSearchQuery.SortOptions.DISTANCE
            R.id.price_radio_button -> ProductSearchQuery.SortOptions.PRICE
            else -> ProductSearchQuery.SortOptions.NAME
        }
    }

    private fun mapSortOptionToId(sortOption: ProductSearchQuery.SortOptions): Int {
        return when (sortOption) {
            ProductSearchQuery.SortOptions.NAME -> R.id.name_radio_button
            ProductSearchQuery.SortOptions.DISTANCE -> R.id.distance_radio_button
            ProductSearchQuery.SortOptions.PRICE -> R.id.price_radio_button
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(applyFilterListener: OnApplyQueryListener) = FilterOffersFragment(applyFilterListener)
    }

    interface OnApplyQueryListener {
        fun onApplyQuery(searchQuery: ProductSearchQuery)
    }
}