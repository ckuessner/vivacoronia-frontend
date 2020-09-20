package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_game_table")
data class QuizGame(
    @PrimaryKey val gameId: String,
    var finishedAt: Long = 0
)