package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentOfferOverviewBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs.NeedsAdapter
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

private const val TAG = "OfferOverviewFragment"

class ProductOverviewFragment : Fragment() {
    private lateinit var binding: FragmentOfferOverviewBinding
    private lateinit var viewModel: ProductListViewModel
    private var showOffers: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showOffers = arguments?.getBoolean("offers")!!

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offer_overview, container, false)
        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)

        if (showOffers) initOfferView()
        else initNeedView()

        BaseProduct.categories.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty())
                binding.add.isEnabled = true
        })
        binding.add.isEnabled = !BaseProduct.categories.value.isNullOrEmpty() // Only allow adding offers if we fetched + received categories

        binding.productListSwipeRefresh.setOnRefreshListener {
            GlobalScope.launch { if (showOffers) fetchMyOffers() else fetchMyNeeds()}
        }

        binding.add.setOnClickListener {
            SubmitOfferActivity.start(
                requireContext(),
                null
            )
        }

        return binding.root
    }

    private fun initOfferView(){
        val adapter =
            OffersAdapter(
                ::deleteOfferCallback,
                ::editOfferCallback
            )
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0)
                    binding.productList.layoutManager!!.smoothScrollToPosition(binding.productList, null, 0)
            }
        })

        binding.productList.adapter = adapter
        viewModel.offers.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })
    }

    private fun initNeedView(){
        val adapter = NeedsAdapter(::deleteOfferCallback)
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0)
                    binding.productList.layoutManager!!.smoothScrollToPosition(binding.productList, null, 0)
            }
        })

        binding.productList.adapter = adapter
        viewModel.needs.observe(viewLifecycleOwner, Observer { it?.let { adapter.submitList(it) } })
    }

    override fun onResume() {
        super.onResume()
        binding.productListSwipeRefresh.post {
            binding.productListSwipeRefresh.isRefreshing = true
            GlobalScope.launch { if (showOffers) fetchMyOffers() else fetchMyNeeds() }
        }
    }

    private fun deleteOfferCallback(id: String) {
        activity?.let {
            AlertDialog.Builder(it, R.style.AlertDialogTheme)
                .setCancelable(true)
                .setPositiveButton(R.string.yes) { _, _ ->
                    binding.productListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, false)
                    }
                }
                .setNegativeButton(R.string.yes_offer_sold) { _, _ ->
                    binding.productListSwipeRefresh.isRefreshing = true
                    GlobalScope.launch {
                        performDelete(id, true)
                    }
                }
                .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setTitle(R.string.delete_offer_title)
                .setMessage(R.string.confirm_delete_message)
                .show()
        } ?: return
    }

    private fun performDelete(id: String, sold: Boolean) {
        try {
            val deleted = TradingApiClient.deleteOffer(id, sold, requireContext())
            if (deleted)
                fetchMyOffers()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to delete offer with id \"$id\"", e)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Unable to delete offer", Toast.LENGTH_SHORT).show()
            }
        }
        activity?.runOnUiThread { binding.offersListSwipeRefresh.isRefreshing = false }
    }

    private fun editOfferCallback(offer: Offer){
        SubmitOfferActivity.start(
            requireContext(),
            offer
        )
    }

    private fun fetchCategories() {
        try {
            val categories = TradingApiClient.getAllCategories(requireContext()).toMutableList()
            requireActivity().runOnUiThread {
                BaseProduct.categories.value = categories
            }
        } catch (e: Exception) {
            // Don't care if we already have categories
            if (!BaseProduct.categories.value.isNullOrEmpty())
                return

            if (requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    activity?.let {
                        val dialog = AlertDialog.Builder(it)
                            .setMessage("Please make sure you have a working Internet connection and try again.")
                            .setTitle("No Internet")
                            .setCancelable(false)
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()}
                            .show()
                        styleDialogButtons(listOf(dialog.getButton(AlertDialog.BUTTON_POSITIVE))) // TODO löschen wenn styling auch ohne so funktioniert wie bei Timo
                    }
                }
        }
    }

    private fun fetchMyOffers() {
        fetch(false)
    }

    private fun fetchMyNeeds() {
        fetch(true)
    }

    private fun fetch(needs: Boolean){
        try {
            if (!needs) {
                activity?.let {
                    val offers = TradingApiClient.getMyOffers(requireContext())
                    it.runOnUiThread { viewModel.setOffers(offers) }
                }
            }
            else {
                val remoteNeeds = TradingApiClient.getMyNeeds(requireContext())
                requireActivity().runOnUiThread { viewModel.setNeeds(remoteNeeds) }
            }
        } catch (exception: ExecutionException) {
            if (exception.cause is VolleyError && activity != null && requireActivity().hasWindowFocus())
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), R.string.server_connection_failed, Toast.LENGTH_LONG).show()
                }
            else {
                Log.e(TAG, "Error while fetching or parsing my Offers of Needs", exception)
            }
        }
        activity?.runOnUiThread { binding.offersListSwipeRefresh.isRefreshing = false }
    }
}