package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import java.lang.IllegalArgumentException
import java.text.NumberFormat
import java.util.*
import kotlin.math.*

class OfferListViewModel : ViewModel() {
    val offers = MutableLiveData<MutableList<OfferViewModel>>()

    fun setOffers(list: List<Offer>) {
        offers.value = MutableList(list.size) { index ->
            OfferViewModel(
                list[index]
            )
        }
    }

    fun setDistances(location: LatLng?) {
        if(location != null) {
            for (offer in offers.value!!) {
                offer.offer.distance = String.format(Locale.US, "%.3f", getCoordinateDistanceOnSphere(offer.offer.location, location)).toDouble()
            }
        }
    }

    private fun getCoordinateDistanceOnSphere(
        startLocation: LatLng,
        endLocation: LatLng
    ): Double {
        val lon1 = Math.toRadians(startLocation.longitude)
        val lat1 = Math.toRadians(startLocation.latitude)
        val lon2 = Math.toRadians(endLocation.longitude)
        val lat2 = Math.toRadians(endLocation.latitude)

        //Haversine formula, determines the great-circle distance between two points on a sphere with given longitude and latitude
        val deltaLon = lon2 - lon1
        val deltaLat = lat2 - lat1
        val innerFormula =
            sin(deltaLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2.0)
        val outerFormula = 2 * asin(sqrt(innerFormula))

        //radius of the earth in kilometers
        val radius = 6371
        return outerFormula * radius
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

    var product: String
        get() = offer.product
        set(value){
            offer.product = value
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

    var productCategory: String
        get() = offer.productCategory
        set(value){
            offer.productCategory = value
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