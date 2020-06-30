package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.location.Location
import java.util.*

class Offer(
    var productName: String,
    var amount: Int,
    var priceTotal: Double,
    var location: Location,
    var details: String,
    var id: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        return other != null
                && other is Offer
                && other.amount == amount
                && other.priceTotal == priceTotal
                && other.location == location
                && other.details == details
                && other.id == id
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, priceTotal, location, details, id)
    }
}