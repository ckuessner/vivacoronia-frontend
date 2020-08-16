package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.Json
import com.google.android.gms.maps.model.LatLng
import java.util.*

class Offer(
    var product: String,
    var amount: Int,
    var priceTotal: Double,
    var location: LatLng,
    var details: String,
    @Json(name="_id") var id: String,
    var productCategory: String
) : Parcelable {
    var distance: Double = 0.0

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor() : this("", 0, 0.0, LatLng(0.0, 0.0), "", "", if (!categories.value.isNullOrEmpty()) categories.value!![0] else "")

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
                && other.productCategory == productCategory
                && other.product == product
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, priceTotal, location, details, id, productCategory, product)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(product)
        parcel.writeInt(amount)
        parcel.writeDouble(priceTotal)
        parcel.writeParcelable(location, flags)
        parcel.writeString(details)
        parcel.writeString(id)
        parcel.writeString(productCategory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        var categories = MutableLiveData<MutableList<String>>()

        @Suppress("unused") // The creator is necessary to implement Parcelable
        @JvmField val CREATOR = object : Parcelable.Creator<Offer> {
            override fun createFromParcel(parcel: Parcel): Offer {
                return Offer(parcel)
            }

            override fun newArray(size: Int): Array<Offer?> {
                return arrayOfNulls(size)
            }
        }
    }
}