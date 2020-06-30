package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

class OfferListViewModel : ViewModel() {
    val offers = MutableLiveData<MutableList<OfferViewModel>>()

    fun setOffers(list: List<Offer>) {
        offers.value = MutableList(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        }
    }

    fun add(list: List<Offer>) {
        val value = this.offers.value ?: mutableListOf()
        value.addAll(List(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        })
        offers.value = value
    }

    fun remove(id: String) {
        val value = this.offers.value ?: mutableListOf()
        val newValue = value.toMutableList()
        newValue.removeIf { offer -> offer.offer.id == id}
        offers.value = newValue // Set to a copy of value in order to notify observers
    }
}

class OfferViewModel(val offer: Offer) {
    var rotation = 0L
}