package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import java.util.*

class Offer(
    product: String,
    amount: Int,
    var price: Double,
    location: LatLng,
    var details: String,
    id: String,
    productCategory: String,
    var distanceToUser: Double,
    var phoneNumber: String
) : BaseProduct(product, productCategory, amount, location, id), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!
    )

    constructor() : this("", 0, 0.0, LatLng(0.0, 0.0), "", "", if (!categories.value.isNullOrEmpty()) categories.value!![0] else "", -1.0, "")

    fun getDistanceToUser(): String {
        if (distanceToUser == -1.0 || location == LatLng(0.0, 0.0))
            return "n/a"
        return String.format(Locale.GERMAN, "%.3f", distanceToUser)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        return other != null
                && other is Offer
                && other.amount == amount
                && other.price == price
                && other.location == location
                && other.details == details
                && other.id == id
                && other.productCategory == productCategory
                && other.product == product
                && other.phoneNumber == phoneNumber
                && other.distanceToUser == distanceToUser
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, price, location, details, id, productCategory, product, phoneNumber)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(product)
        parcel.writeInt(amount)
        parcel.writeDouble(price)
        parcel.writeParcelable(location, flags)
        parcel.writeString(details)
        parcel.writeString(id)
        parcel.writeString(productCategory)
        parcel.writeDouble(distanceToUser)
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused") // The creator is necessary to implement Parcelable
        @JvmField
        val CREATOR = object : Parcelable.Creator<Offer> {
            override fun createFromParcel(parcel: Parcel): Offer {
                return Offer(parcel)
            }

            override fun newArray(size: Int): Array<Offer?> {
                return arrayOfNulls(size)
            }
        }
    }
}