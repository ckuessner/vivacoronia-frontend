package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.QuizGameViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Answer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.OpponentInfo
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Question
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.QuizGameDto

object QuizClient {
    fun postGame(): QuizGameDto {
        TODO("Initialize new game")
    }

    fun getGame(gameId: String): QuizGameViewModel {
        TODO("Not yet implemented")
    }

    fun getFinishedGamesDummy(gameIds: List<String>): List<QuizGameViewModel> {
        /*val questions = listOf(Question("Was ist das für eine Frage?", listOf("Eine gute Frage", "Das ist keine Frage", "Frag was anderes", "Du bist doof"), "Du bist doof"))
        val answers = listOf<Answer>()
        return listOf(
            QuizGameViewModel(QuizGameDto("1", questions, answers, OpponentInfo("2", 300)))
        )*/
        return listOf()
    }

    fun getGames(gameIds: List<String>): List<QuizGameViewModel> {
        val questions = listOf(
            Question("Was ist das für eine Frage?", listOf("Eine gute Frage", "Das ist keine Frage", "Frag was anderes", "Du bist doof"), "Du bist doof"),
            Question("Was ist das für eine Frage?", listOf("Eine gute Frage", "Das ist keine Frage", "Frag was anderes", "Du bist doof"), "Du bist doof"),
            Question("Was ist das für eine Frage?", listOf("Eine gute Frage", "Das ist keine Frage", "Frag was anderes", "Du bist doof"), "Du bist doof"),
            Question("Was ist das für eine Frage?", listOf("Eine gute Frage", "Das ist keine Frage", "Frag was anderes", "Du bist doof"), "Du bist doof")
        )
        val answers = listOf(
            Answer("1", 0, "Frag was anderes", false),
            Answer("2", 0, "Du bist doof", true),
            Answer("1", 1, "Du bist doof", true),
            Answer("2", 1, "Du bist doof", true),
            Answer("1", 2, "Du bist doof", true),
            Answer("2", 2, "Frag was anderes", false),
            Answer("1", 3, "Frag was anderes", false)
        )
        return listOf(
            QuizGameViewModel(QuizGameDto("1", questions, answers, OpponentInfo("2", 300)))
        )
    }

    fun postAnswer(gameId: String, questionIndex: Int, answer: String) {}
}