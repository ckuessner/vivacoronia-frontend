package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentOfferDetailBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Category
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

private const val ARG_OFFER = "offer"

class OfferDetailFragment : Fragment() {
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
        if (this::viewModel.isInitialized)
            binding.offer = viewModel
            binding.categoryInputSpinner.setSelection(categories.indexOfFirst { it.name == viewModel.category })

        binding.categoryInputSpinner.adapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, categories)
        return binding.root
    }

    fun getOffer(): Offer {
        return viewModel.offer
    }

    companion object {
        @JvmStatic
        fun newInstance(offer: Offer) =
            OfferDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OFFER, offer)
                }
            }
        var categories = listOf<Category>()
    }
}