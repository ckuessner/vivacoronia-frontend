package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class SupermarketInventoryItemViewModel(var inventoryItem: InventoryItem) : ViewModel() {
    var id: String
        get() = inventoryItem.id
        set(value) {
            inventoryItem.id = value
        }

    var name: String
        get() = inventoryItem.itemName
        set(value){
            inventoryItem.itemName = value
        }

    var availabilityLevel: String
        get() = availabilityLevelToText(inventoryItem.availability)
        set(value) {
            inventoryItem.availability = availabilityTextToLevel(value)
        }

    var productCategory: String
        get() = inventoryItem.productCategory
        set(value) {
            inventoryItem.productCategory = value
        }

    var supermarket: Supermarket?
        get() = inventoryItem.supermarket
        set(value) {
            inventoryItem.supermarket = value
        }

    private fun availabilityLevelToText(availabilityLevel: Int): String {
        return when(availabilityLevel){
            0 -> "Unavailable"
            1 -> "Small amount"
            2 -> "Medium amount"
            3 -> "Large amount"
            else -> throw IllegalStateException()
        }
    }

    private fun availabilityTextToLevel(availabilityText: String): Int {
        return when(availabilityText){
            "Unavailable" -> 0
            "Small amount" -> 1
            "Medium amount" -> 2
            "Large amount" -> 3
            else -> throw IllegalStateException()
        }
    }
}

class InventoryItemViewModelFactory(private val inventoryItem: InventoryItem): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SupermarketInventoryItemViewModel::class.java)){
            return SupermarketInventoryItemViewModel(inventoryItem) as T
        }
        throw IllegalArgumentException("Unknown SupermarketInventoryItemViewModel class")
    }
}