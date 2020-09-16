package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

class ProductSearchQuery (
                          var productName: String,
                          var category: String,
                          var amountMin: String,
                          var location: LatLng?,
                          var radiusInKm: Int ) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(LatLng::class.java.classLoader),
        parcel.readInt()
    )

    enum class SortOptions (val attribute: String) {
        NAME("name"),
        DISTANCE("distance"),
        PRICE("price")
    }

    constructor() : this( "", "", "", null, 0)

    var userId: String = ""
    var priceMin: String = ""
    var priceMax: String = ""
    var sortBy: SortOptions = SortOptions.NAME

    override fun toString(): String {
        val builder = Uri.Builder()
        if (userId.isNotEmpty())
            builder.appendQueryParameter("userId", userId)
        if (productName.isNotEmpty())
            builder.appendQueryParameter("product", productName)
        if (category.isNotEmpty())
            builder.appendQueryParameter("productCategory", category)
        if (priceMin.isNotEmpty())
            builder.appendQueryParameter("priceMin", priceMin.replace(",", "."))
        if (priceMax.isNotEmpty())
            builder.appendQueryParameter("priceMax", priceMax.replace(",", "."))
        if (amountMin.isNotEmpty())
            builder.appendQueryParameter("amountMin", amountMin)
        if (location != null) {
            builder.appendQueryParameter("longitude", location!!.longitude.toString())
            builder.appendQueryParameter("latitude", location!!.latitude.toString())
        }
        if (radiusInKm != 0)
            builder.appendQueryParameter("radiusInKm", radiusInKm.toString())

        builder.appendQueryParameter("sortBy", sortBy.attribute)
        return builder.toString().replaceFirst("?", "")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(productName)
        dest.writeString(category)
        dest.writeString(amountMin)
        dest.writeParcelable(location, flags)
        dest.writeInt(radiusInKm)
        dest.writeString(priceMin)
        dest.writeString(priceMax)
        dest.writeString(sortBy.attribute)
        dest.writeString(userId)
    }

    companion object CREATOR : Parcelable.Creator<ProductSearchQuery> {
        override fun createFromParcel(parcel: Parcel): ProductSearchQuery {
            return ProductSearchQuery(parcel)
        }

        override fun newArray(size: Int): Array<ProductSearchQuery?> {
            return arrayOfNulls(size)
        }
    }
}