package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Category
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
        val oldList = this.offers.value ?: mutableListOf()
        val newList = oldList.toMutableList()
        val index = newList.indexOfFirst { it.offer.id == id }
        if (index != -1)
            newList.removeAt(index)
        offers.value = newList // Set to a copy of value in order to notify observers
    }
}

class OfferViewModel(var offer: Offer) : ViewModel() {
    object CurrencyFormatter {
        // replacing "space" (\u0020) with "non-breaking space"(\u00A0), important to be able to parse!
        private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY)

        init {
            currencyFormatter.maximumFractionDigits = 2
        }

        fun format(amount: Double): String {
            return currencyFormatter.format(amount).trim()
        }

        fun formatWithoutCurrency(amount: Double): String {
            return if (amount == 0.0) "" else currencyFormatter.format(amount).replace("\u00A0€", "")
        }

        fun parse(amount: String): Double {
            return currencyFormatter.parse(amount.trim())!!.toDouble()
        }

        fun parseWithoutCurrency(amount: String): Double {
            if(amount == "") return 0.0
            @Suppress("NAME_SHADOWING")
            val amount = amount.replace("\u0020", "\u00A0").replace(".", ",").trim() + "\u00A0€"
            return currencyFormatter.parse(amount)!!.toDouble()
        }
    }

    var rotation = 0L
    val isExpanded: Boolean
        get() = rotation != 0L

    var productName: String
        get() = offer.productName
        set(value){
            offer.productName = value
        }

    var rawPrice: String
        get() {
            return CurrencyFormatter.formatWithoutCurrency(offer.priceTotal)
        }
        set(value) {
            offer.priceTotal = CurrencyFormatter.parseWithoutCurrency(value)
        }

    var priceWithCurrency: String
        get() {
            return CurrencyFormatter.format(offer.priceTotal)
        }
        set(value) {
            offer.priceTotal = CurrencyFormatter.parse(value)
        }

    var details: String
        get() = offer.details
        set(value){
            offer.details = value
        }

    var amount: String
        get() = if (offer.amount == 0) "" else offer.amount.toString()
        set(value){
            offer.amount = if (value == "") 0 else value.toInt()
        }

    var category: String
        get() = offer.category.toString()
        set(value){
            offer.category = Category.getCategoryByName(value)
        }
}

class OfferViewModelFactory(private val offer: Offer): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OfferViewModel::class.java)) {
            return OfferViewModel(offer) as T
        }
        throw IllegalArgumentException("Unknown OfferViewModel class")
    }

}