package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.location.Location

class Offer(
    var productName: String,
    var amount: Int,
    var priceTotal: Double,
    var location: Location,
    var details: String
) {

}