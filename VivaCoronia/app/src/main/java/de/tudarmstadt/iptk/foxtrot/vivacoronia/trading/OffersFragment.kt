package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentOfferBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

private const val TAG = "OffersFragment"

class OffersFragment : Fragment() {

    companion object {
        fun newInstance() = OffersFragment()
    }

    private lateinit var binding: FragmentOfferBinding
    private lateinit var viewModel: OffersViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_offer,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(OffersViewModel::class.java)
        val adapter = OffersAdapter()
        binding.offersList.adapter = adapter
        viewModel.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.data = it }})

        GlobalScope.launch { fetchMyOffers() }

        return binding.root
    }

    private fun fetchMyOffers() {
        try {
            val offers = TradingApiClient.getMyOffers()
            requireActivity().runOnUiThread { viewModel.offers.value = offers }
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }
            else {
                Log.e(TAG, "Error while fetching or parsing myOffers", exception)
            }
        }
    }
}