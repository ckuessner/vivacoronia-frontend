package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.QuizGameViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.QuizGameDto

object QuizClient {
    fun postGame(): QuizGameDto {
        TODO("Initialize new game")
    }

    fun getGame(gameId: String, location: LatLng): QuizGameViewModel {
        TODO("Not yet implemented")
    }

    fun getFinishedGamesDummy(gameIds: List<String>): List<QuizGameViewModel> {
        val finishedGames = listOf(
            QuizGameViewModel("1", "1", 3f, -1),
            QuizGameViewModel("2", "2", 5f, -1),
            QuizGameViewModel("3", "3", 7f, -1),
            QuizGameViewModel("4", "2", 9f, -1),
            QuizGameViewModel("5", "1", 11f, -1)

        )
        finishedGames[0].gameState = QuizGameViewModel.GameState.DRAW
        finishedGames[1].gameState = QuizGameViewModel.GameState.WON
        finishedGames[2].gameState = QuizGameViewModel.GameState.WON
        finishedGames[3].gameState = QuizGameViewModel.GameState.LOST
        finishedGames[4].gameState = QuizGameViewModel.GameState.DRAW
        return finishedGames
    }

    fun getGames(gameIds: List<String>): List<QuizGameViewModel> {
        val openGames = listOf(
            QuizGameViewModel("1", "1", 3f, -1),
            QuizGameViewModel("2", "2", 5f, -1),
            QuizGameViewModel("3", "3", 7f, -1),
            QuizGameViewModel("4", "2", 9f, -1),
            QuizGameViewModel("5", "1", 11f, -1)

        )
        openGames.forEach { it.gameState = QuizGameViewModel.GameState.OPEN }
        return openGames
    }

    fun postAnswer(gameId: String, questionIndex: Int, answer: String) {}
}