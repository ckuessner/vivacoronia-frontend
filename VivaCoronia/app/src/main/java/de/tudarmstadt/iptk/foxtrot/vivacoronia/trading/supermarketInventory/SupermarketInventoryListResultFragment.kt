package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.volley.VolleyError
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSupermarketInventoryListResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SupermarketInventoryListResultFragment(private val parent: SupermarketInventoryFragment) : Fragment() {
    private lateinit var binding: FragmentSupermarketInventoryListResultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_supermarket_inventory_list_result,
            container,
            false
        )
        binding.supermarketName.text = "Please select a supermarket first by long pressing on the map"

        val adapter = SupermarketInventoryResultAdapter(SupermarketItemListener { item: SupermarketInventoryItemViewModel, availability: Int ->
            editInventoryItem(
                item,
                availability
            )
        })
        binding.inventoryList.adapter = adapter
        parent.inventoryViewModel.supermarketInventory.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.supermarketName.text = it.supermarketName
                binding.listSupermarketDetails.isVisible = true
                binding.viewOnMap.isVisible = true
                binding.supermarketInventoryEditButton.isVisible = true
                adapter.submitList(it.inventoryViewModel)
            }
        })
        binding.viewOnMap.setOnClickListener {
            switchToMap(parent.inventoryViewModel.supermarketInventory.value?.supermarketId)
        }
        binding.supermarketInventoryEditButton.setOnClickListener {
            if(parent.inventoryViewModel.supermarketInventory.value != null){
                editInventory(parent.inventoryViewModel.supermarketInventory.value!!)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        reloadCurrentSupermarket()
    }

    private fun editInventoryItem(item: SupermarketInventoryItemViewModel, availability: Int) {
        GlobalScope.launch {
            try {
                requireActivity().runOnUiThread {
                    TradingApiClient.putInventoryItem(
                        item.inventoryItem,
                        newItem = false,
                        newSupermarket = false,
                        availability = availability,
                        context = requireContext()
                    )
                }
            }
            catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Log.e("S.Inv.ListResultFrag", "Error editing or adding inventory item: ", e)
                    Toast.makeText(requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        Handler().postDelayed(this::reloadCurrentSupermarket, 500)
    }

    private fun editInventory(supermarket: Supermarket) {
        EditInventoryItemActivity.start(
            requireContext(), InventoryItem(
                "",
                "",
                "",
                0,
                supermarket.supermarketId,
                supermarket.supermarketName,
                supermarket.supermarketLocation
            ), newItem = true, newSupermarket = false
        )
    }

    private fun switchToMap(supermarketId: String?) {
        if(supermarketId!=null){
            parent.searchViewModel.selectedMarker.value = supermarketId
            parent.switchFragments(0)
        }
    }

    private fun reloadCurrentSupermarket() {
        if (parent.inventoryViewModel.supermarketInventory.value != null) {
            GlobalScope.launch {
                val id = parent.inventoryViewModel.supermarketInventory.value!!.supermarketId
                val name = parent.inventoryViewModel.supermarketInventory.value!!.supermarketName
                val location = parent.inventoryViewModel.supermarketInventory.value!!.supermarketLocation
                val response: Supermarket =
                    TradingApiClient.getSupermarketInventoryForID(
                        requireContext(),
                        PlacesApiResult(id, name, location),
                        ::onRequestFailed
                    )
                requireActivity().runOnUiThread {
                    parent.inventoryViewModel.supermarketInventory.value = response
                }
            }
        }
    }

    private fun onRequestFailed(error: VolleyError, supermarket: PlacesApiResult){
        error.printStackTrace()
    }

    companion object {
        @JvmStatic
        fun newInstance(parent: SupermarketInventoryFragment) = SupermarketInventoryListResultFragment(
            parent
        )
    }
}