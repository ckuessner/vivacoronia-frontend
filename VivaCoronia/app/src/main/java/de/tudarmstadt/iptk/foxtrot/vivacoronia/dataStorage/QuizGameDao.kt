package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage

import androidx.room.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame

@Dao
interface QuizGameDao {
    @Insert
    fun insert(game: QuizGame)

    @Update
    fun update(game: QuizGame)

    @Delete
    fun delete(game: QuizGame)

    @Query("SELECT * FROM quiz_game_table WHERE finishedAt == -1")
    fun getActive(): List<QuizGame>

    @Query("SELECT * FROM quiz_game_table WHERE finishedAt > 0")
    fun getFinished(): List<QuizGame>

    @Query("SELECT * FROM quiz_game_table WHERE gameId = :gameId")
    fun getGame(gameId: String): QuizGame?
}