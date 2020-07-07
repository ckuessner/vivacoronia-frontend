package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Offer(
    var productName: String,
    var amount: Int,
    var priceTotal: Double,
    var location: Location,
    var details: String,
    var id: String,
    var category: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor() : this("", 0, 0.0, Location(""), "", "", "")

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
                && other.category == category
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, priceTotal, location, details, id, category)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productName)
        parcel.writeInt(amount)
        parcel.writeDouble(priceTotal)
        parcel.writeParcelable(location, flags)
        parcel.writeString(details)
        parcel.writeString(id)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Offer> {
        override fun createFromParcel(parcel: Parcel): Offer {
            return Offer(parcel)
        }

        override fun newArray(size: Int): Array<Offer?> {
            return arrayOfNulls(size)
        }
    }
}

class Category(val name: String, val subCategories: List<Category>)