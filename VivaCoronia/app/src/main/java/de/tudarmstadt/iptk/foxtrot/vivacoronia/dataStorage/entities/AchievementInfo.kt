package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants

@Entity
data class AchievementInfo(
    @PrimaryKey val achievement: String,
    val type: String,
    val neededForHigher : Int,
    val percentageOfPeople : Int
)