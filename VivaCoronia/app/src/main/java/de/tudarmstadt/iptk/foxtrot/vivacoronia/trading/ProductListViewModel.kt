package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.util.*

open class ProductListViewModel : ViewModel() {
    val offers = MutableLiveData<MutableList<OfferViewModel>>()
    val needs = MutableLiveData<MutableList<NeedViewModel>>()


    fun setOffers(list: List<Offer>) {
        offers.value = MutableList(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        }
    }

    fun setNeeds(list: List<Need>) {
        needs.value = MutableList(list.size) { index ->
            NeedViewModel(list[index])
        }
    }

    fun addOffer(list: List<Offer>) {
        val value = this.offers.value ?: mutableListOf()
        value.addAll(List(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        })
        offers.value = value
    }
}

open class ProductViewModel(var baseProduct: BaseProduct) : ViewModel(){

    var product: String
        get() = baseProduct.product
        set(value){
            baseProduct.product = value
        }

    var productCategory: String
        get() = baseProduct.productCategory
        set(value){
            baseProduct.productCategory = value
        }

}

class NeedViewModel(var need: Need) : ProductViewModel(need as BaseProduct)


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

    var rotation = 0L
    val isExpanded: Boolean
        get() = rotation != 0L

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

    var amount: String
        get() = if (offer.amount == 0) "" else offer.amount.toString()
        set(value){
            offer.amount = if (value == "") 0 else value.toInt()
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

class NeedViewModelFactory(private val need: Need): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NeedViewModel::class.java)) {
            return NeedViewModel(need) as T
        }
        throw IllegalArgumentException("Unknown NeedViewModel class")
    }
}