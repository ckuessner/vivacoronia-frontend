package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Category.Companion.categories
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Category.Companion.getCategoryByName
import java.lang.Exception
import java.util.*

class Offer(
    var productName: String,
    var amount: Int,
    var priceTotal: Double,
    var location: Location,
    var details: String,
    var id: String,
    var category: Category
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readParcelable(Location::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        getCategoryByName(parcel.readString()!!)
    )

    constructor(productName: String, amount: Int, priceTotal: Double, location: Location, details: String, id: String, category: String)
            : this(productName, amount, priceTotal, location, details, id, getCategoryByName(category))

    constructor() : this("", 0, 0.0, Location(""), "", "", categories[0])

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
                && other.category.name == category.name
    }

    override fun hashCode(): Int {
        return Objects.hash(amount, priceTotal, location, details, id, category.name)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productName)
        parcel.writeInt(amount)
        parcel.writeDouble(priceTotal)
        parcel.writeParcelable(location, flags)
        parcel.writeString(details)
        parcel.writeString(id)
        parcel.writeString(category.name)
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

class Category(val name: String, val subCategories: List<Category>) {
    companion object {
        var categories = mutableListOf(Category(""), Category("Hygiene"), Category("Lebensmittel"), Category("Sonstiges"))

        fun getCategoryByName(name: String): Category {
            return try {
                categories.first {it.name == name }
            } catch (e: Exception) {
                val category = Category(name)
                categories.add(category)
                category
            }
        }
    }

    constructor(name: String) : this(name, listOf())

    override fun toString(): String {
        return if (name != "") name else "No Category"
    }
}