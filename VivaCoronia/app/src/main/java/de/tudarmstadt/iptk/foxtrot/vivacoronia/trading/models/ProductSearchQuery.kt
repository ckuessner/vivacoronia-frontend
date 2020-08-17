package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

class ProductSearchQuery {
    enum class SortOptions (val attribute: String) {
        NAME("name"),
        DISTANCE("distance"),
        PRICE("price")
    }

    var userId: Int? = null
    var productName: String? = null
    var category: String = ""
    var priceMin: String = ""
    var priceMax: String = ""
    var location: LatLng? = null
    var radiusInKm: Int = 0
    var sortBy: SortOptions = SortOptions.NAME

    override fun toString(): String {
        val builder = Uri.Builder()
        if (userId != null)
            builder.appendQueryParameter("userId", userId.toString())
        if (productName != null)
            builder.appendQueryParameter("product", productName)
        if (category.isNotEmpty())
            builder.appendQueryParameter("productCategory", category)
        if (priceMin.isNotEmpty())
            builder.appendQueryParameter("priceMin", priceMin)
        if (priceMax.isNotEmpty())
            builder.appendQueryParameter("priceMax", priceMax)
        if (location != null) {
            builder.appendQueryParameter("longitude", location!!.longitude.toString())
            builder.appendQueryParameter("latitude", location!!.latitude.toString())
        }
        if (radiusInKm != 0)
            builder.appendQueryParameter("radius", radiusInKm.toString())

        builder.appendQueryParameter("sortBy", sortBy.attribute)
        return builder.toString().replaceFirst("?", "")
    }
}