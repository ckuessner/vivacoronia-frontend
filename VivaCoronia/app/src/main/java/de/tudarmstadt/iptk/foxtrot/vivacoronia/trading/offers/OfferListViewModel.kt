package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.ProductViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.util.*

class OfferListViewModel : ViewModel() {
    val offersList = MutableLiveData<MutableList<OfferViewModel>>()

    fun setOffers(list: List<Offer>) {
        offersList.value = MutableList(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        }
    }

    fun add(list: List<Offer>) {
        val value = this.offersList.value ?: mutableListOf()
        value.addAll(List(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        })
        offersList.value = value
    }
}

class OfferViewModel(var offer: Offer) : ProductViewModel(offer as BaseProduct) {
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

    var rawPrice: String
        get() {
            return CurrencyFormatter.formatWithoutCurrency(offer.price)
        }
        set(value) {
            offer.price = CurrencyFormatter.parseWithoutCurrency(value)
        }

    var priceWithCurrency: String
        get() {
            return CurrencyFormatter.format(offer.price)
        }
        set(value) {
            offer.price = CurrencyFormatter.parse(value)
        }

    var details: String
        get() = offer.details
        set(value){
            offer.details = value
        }

    var phoneNumber: String
        get() = offer.phoneNumber
        set(value){
            offer.phoneNumber = value
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
