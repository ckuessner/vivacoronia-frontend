package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

class Supermarket(
    val supermarketId: String,
    val supermarketName: String,
    val supermarketLocation: LatLng,
    var inventory: List<InventoryItem>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(LatLng::class.java.classLoader)!!,
        parcel.createTypedArrayList(InventoryItem.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(supermarketId)
        parcel.writeString(supermarketName)
        parcel.writeParcelable(supermarketLocation, flags)
        parcel.writeTypedList(inventory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Supermarket> {
        override fun createFromParcel(parcel: Parcel): Supermarket {
            return Supermarket(parcel)
        }

        override fun newArray(size: Int): Array<Supermarket?> {
            return arrayOfNulls(size)
        }
    }
}

class InventoryItem : Parcelable {
    var supermarket: Supermarket? = null
    var availability: Int
    var itemName: String

    constructor(itemName: String,
                availability: Int,
                supermarketName: String?,
                supermarketId: String?,
                supermarketLocation: LatLng?){
        this.itemName = itemName
        this.availability = availability
        if(!supermarketId.isNullOrEmpty() && !supermarketName.isNullOrEmpty() && supermarketLocation!=null){
            this.supermarket = Supermarket(supermarketId, supermarketName, supermarketLocation, listOf())
        }
    }

    constructor(parcel: Parcel): this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(LatLng::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemName)
        parcel.writeInt(availability)
        parcel.writeParcelable(supermarket, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InventoryItem> {
        override fun createFromParcel(parcel: Parcel): InventoryItem {
            return InventoryItem(parcel)
        }

        override fun newArray(size: Int): Array<InventoryItem?> {
            return arrayOfNulls(size)
        }
    }
}