package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.util.*

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
        newValue.removeIf { offer -> offer.offer.id == id }
        offers.value = newValue // Set to a copy of value in order to notify observers
    }
}

class OfferViewModel(var offer: Offer) : ViewModel() {
    object CurrencyFormatter {
        private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance()

        init {
            currencyFormatter.maximumFractionDigits = 2
            currencyFormatter.currency = Currency.getInstance("EUR")
        }

        fun format(amount: Double): String {
            return currencyFormatter.format(amount)
        }
    }

    var rotation = 0L
    val isExpanded: Boolean
        get() = rotation != 0L

    val productName: String
        get() = offer.productName
    val price: String
        get() {
            return CurrencyFormatter.format(offer.priceTotal)
        }
    val details: String
        get() = offer.details
    val amount: String
        get() = offer.amount.toString()

    val category: String
        get() = offer.category
}

class OfferViewModelFactory(private val offer: Offer): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfferViewModel::class.java)) {
            return OfferViewModel(offer) as T
        }
        throw IllegalArgumentException("Unknown OfferViewModel class")
    }

}