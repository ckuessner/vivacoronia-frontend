package de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DBLocation (
    @PrimaryKey val time: String,
    val longitude: Double,
    val latitude: Double
)