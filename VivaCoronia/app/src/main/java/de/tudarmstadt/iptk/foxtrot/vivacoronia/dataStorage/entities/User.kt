package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val userID: Int,
    val name: String?
)