package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.Json
import com.google.android.gms.maps.model.LatLng

open class BaseProduct (
    open var product: String,
    open var productCategory: String,
    open var amount: Int,
    open var location: LatLng,
    @Json(name="_id") open var id: String
) {

    companion object {
        var categories = MutableLiveData<MutableList<String>>()

    }
}