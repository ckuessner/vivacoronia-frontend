package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket

class SupermarketInventoryViewModel : ViewModel() {
    val supermarketInventory = MutableLiveData<Supermarket>()
}