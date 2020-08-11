package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct

private const val LOCATION_PICKER_REQUEST = 1


abstract class ProductDetailFragment<T : ProductViewModel> : Fragment() {

    lateinit var viewModel: T

    fun isInitialized(): Boolean {
        return this::viewModel.isInitialized
    }

    fun onSelectLocation() {
        val intent = LocationPickerActivity.getStartIntent(requireActivity(), viewModel.baseProduct.location)
        startActivityForResult(intent, LOCATION_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val latLngResult = LocationPickerActivity.getLatLngResult(data) ?: return
            viewModel.baseProduct.location = latLngResult
        }
    }

    fun getProduct(): BaseProduct {
        return viewModel.baseProduct
    }
}