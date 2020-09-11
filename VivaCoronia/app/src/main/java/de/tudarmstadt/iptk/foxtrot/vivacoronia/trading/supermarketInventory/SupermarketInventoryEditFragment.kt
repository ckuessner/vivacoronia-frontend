package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentSupermarketInventoryEditBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer.Companion.categories

private const val ARG_ITEM = "item"
private const val ARG_NEW = "new"
private const val ARG_NEW_SUPERMARKET = "newSupermarket"

class SupermarketInventoryEditFragment : Fragment() {
    private var newItem = false
    private var newSupermarket = false

    companion object {
        @JvmStatic
        fun newInstance(item: InventoryItem, newItem: Boolean, newSupermarket: Boolean) =
            SupermarketInventoryEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                    putBoolean(ARG_NEW, newItem)
                    putBoolean(ARG_NEW_SUPERMARKET, newSupermarket)
                }
            }
    }

    private lateinit var binding: FragmentSupermarketInventoryEditBinding
    private lateinit var viewModel: SupermarketInventoryItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val item: InventoryItem = it.getParcelable(ARG_ITEM)!!
            newItem = it.getBoolean(ARG_NEW)
            newSupermarket = it.getBoolean(ARG_NEW_SUPERMARKET)
            val viewModelFactory = InventoryItemViewModelFactory(item)
            viewModel = ViewModelProvider(this, viewModelFactory).get(SupermarketInventoryItemViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_supermarket_inventory_edit, container, false)


        val categorySpinnerAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, categories.value!!)
        binding.categoryInputSpinner.adapter = categorySpinnerAdapter
        binding.categoryInputSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.productCategory = parent?.getItemAtPosition(position) as String
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        if(this::viewModel.isInitialized){
            binding.item = viewModel
            val ids = arrayListOf<Int>()
            for(child in binding.availabilityChoices.children) {
                ids.add(child.id)
            }
            binding.availabilityChoices.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if(isChecked){
                    setAvailabilityLevel(ids.indexOf(checkedId))
                }
            }
            val categoryPos = categories.value!!.indexOf(viewModel.inventoryItem.productCategory)
            if(categoryPos != -1){
                binding.categoryInputSpinner.setSelection(categoryPos)
            }
            binding.supermarketName.text = "Item for ${viewModel.supermarket!!.supermarketName}"
            if(viewModel.id != "") {
                binding.productNameInput.isEnabled = false
            }
            if(viewModel.productCategory != ""){
                binding.categoryInputSpinner.isEnabled = false
            }
        }
        return binding.root
    }

    private fun setAvailabilityLevel(availabilityLevel: Int){
        val availabilityLevels = listOf("Unavailable", "Small amount", "Medium amount", "Large amount")
        if(availabilityLevel >= 0 && availabilityLevel < availabilityLevels.size){
            viewModel.availabilityLevel = availabilityLevels[availabilityLevel]
        }
    }

    fun getItem(): ArrayList<Any>? {
        return if(viewModel.inventoryItem.itemName != ""){
            arrayListOf(viewModel.inventoryItem, newItem, newSupermarket)
        }
        else {
            Toast.makeText(requireContext(), "Please enter a product name", Toast.LENGTH_SHORT).show()
            null
        }
    }
}