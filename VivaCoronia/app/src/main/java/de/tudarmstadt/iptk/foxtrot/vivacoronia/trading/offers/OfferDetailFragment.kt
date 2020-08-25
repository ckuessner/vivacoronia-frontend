package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentOfferDetailBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer.Companion.categories

private const val ARG_OFFER = "offer"
private const val LOCATION_PICKER_REQUEST = 1

class OfferDetailFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(offer: Offer) =
            OfferDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OFFER, offer)
                }
            }
    }

    private lateinit var binding: FragmentOfferDetailBinding
    private lateinit var viewModel: OfferViewModel
    private lateinit var viewModelFactory: OfferViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val offer: Offer = it.getParcelable(ARG_OFFER)!!
            viewModelFactory = OfferViewModelFactory(offer)
            viewModel = ViewModelProvider(this, viewModelFactory).get(OfferViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offer_detail, container, false)

        val spinnerAdapter =  ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, categories.value!!)
        binding.categoryInputSpinner.adapter = spinnerAdapter
        binding.categoryInputSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.offer.productCategory = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        if (this::viewModel.isInitialized) {
            binding.offer = viewModel
            binding.categoryInputSpinner.setSelection(spinnerAdapter.getPosition(viewModel.productCategory))
        }

        binding.locationPickerButton.setOnClickListener { onSelectLocation() }

        return binding.root
    }

    fun getOffer(): Offer {
        return viewModel.offer
    }

    private fun onSelectLocation() {
        val intent = LocationPickerActivity.getStartIntent(requireActivity(), viewModel.offer.location, null)
        startActivityForResult(intent, LOCATION_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val latLngResult = LocationPickerActivity.getLatLngResult(data) ?: return
            viewModel.offer.location = latLngResult
        }
    }
}