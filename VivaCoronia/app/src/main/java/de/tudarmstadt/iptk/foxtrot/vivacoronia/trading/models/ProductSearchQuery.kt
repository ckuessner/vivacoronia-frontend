package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

class ProductSearchQuery {
    var userId: Int? = null
    var productName: String? = null
    var category: String? = null
    var price: Double? = null
    var location: LatLng? = null
    var radiusInMeters: Int? = null

    override fun toString(): String {
        val builder = Uri.Builder()
        if (userId != null)
            builder.appendQueryParameter("userId", userId.toString())
        if (productName != null)
            builder.appendQueryParameter("product", productName)
        if (category != null)
            builder.appendQueryParameter("productCategory", category)
        if (price != null)
            TODO("Price is not yet implemented")
        if (location != null) {
            builder.appendQueryParameter("longitude", location!!.longitude.toString())
            builder.appendQueryParameter("latitude", location!!.latitude.toString())
        }
        if (radiusInMeters != null)
            builder.appendQueryParameter("radius", radiusInMeters.toString())
        return builder.toString().replaceFirst("?", "")
    }
}