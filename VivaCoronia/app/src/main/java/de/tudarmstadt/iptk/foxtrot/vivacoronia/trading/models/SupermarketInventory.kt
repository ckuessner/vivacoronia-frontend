package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models

import android.os.Parcel
import android.os.Parcelable

class SupermarketInventory(
    var supermarketId: String,
    var supermarketName: String,
    var inventory: List<InventoryItem>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(InventoryItem.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(supermarketId)
        parcel.writeString(supermarketName)
        parcel.writeTypedList(inventory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SupermarketInventory> {
        override fun createFromParcel(parcel: Parcel): SupermarketInventory {
            return SupermarketInventory(parcel)
        }

        override fun newArray(size: Int): Array<SupermarketInventory?> {
            return arrayOfNulls(size)
        }
    }
}

class InventoryItem(
    var item: Pair<String, Int>
) : Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readSerializable()!! as Pair<String, Int>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(item)
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