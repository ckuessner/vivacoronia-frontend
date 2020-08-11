package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.os.Parcel
import android.os.Parcelable
import com.beust.klaxon.Json
import com.google.android.gms.maps.model.LatLng
import java.util.*

class Need(
    override var product: String,
    override var productCategory: String,
    override var location: LatLng,
    @Json(name="_id") override var id: String
) : BaseProduct(product, productCategory, location, id), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readString()!!
    )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        return other != null
                && other is Need
                && other.product == product
                && other.productCategory == productCategory
                && other.location == location
                && other.id == id
    }

    override fun hashCode(): Int {
        return Objects.hash(location, id, productCategory, product)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(product)
        parcel.writeString(productCategory)
        parcel.writeParcelable(location, flags)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Need> {
        override fun createFromParcel(parcel: Parcel): Need {
            return Need(parcel)
        }

        override fun newArray(size: Int): Array<Need?> {
            return arrayOfNulls(size)
        }
    }
}