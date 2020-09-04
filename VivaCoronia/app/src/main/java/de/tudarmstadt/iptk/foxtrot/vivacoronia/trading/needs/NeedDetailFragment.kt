package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentNeedDetailBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.ProductDetailFragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need

class NeedDetailFragment : ProductDetailFragment<NeedViewModel>() {

    private lateinit var binding: FragmentNeedDetailBinding
    private lateinit var viewModelFactory: NeedViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO get current location
        viewModelFactory =
            NeedViewModelFactory(
                Need("", "", 0, LatLng(0.0, 0.0), "")
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(NeedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_need_detail, container, false)

        val spinnerAdapter =  ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, BaseProduct.categories.value!!)
        binding.categoryInputSpinner.adapter = spinnerAdapter
        binding.categoryInputSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.need.productCategory = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        if (isInitialized()) {
            binding.need = viewModel
            binding.categoryInputSpinner.setSelection(spinnerAdapter.getPosition(viewModel.productCategory))
        }

        binding.locationPickerButton.setOnClickListener { onSelectLocation() }


        return binding.root
    }
}