package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Answer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Question
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.QuizGameDto
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class QuizGameViewModel(
    val quizGame: QuizGameDto
) : ViewModel(), Parcelable {
    enum class GameState {
        WON, LOST, DRAW, OPEN
    }

    @IgnoredOnParcel val opponentName: String
    @IgnoredOnParcel val opponentDistanceInKm: String
    @IgnoredOnParcel var gameState: GameState = GameState.OPEN
    @IgnoredOnParcel var isOpponentsTurn: Boolean = false
    val finished: Boolean
        get() = gameState != GameState.OPEN

    init {
        val faker = Faker(Random(quizGame.opponentInfo.userId.hashCode().toLong()))
        val name = faker.name()
        opponentName = name.firstName()
        opponentDistanceInKm = (quizGame.opponentInfo.distanceInMeters / 1000).toString()

        computeGameState()
    }

    private fun computeGameState() {
        val (opponentAnswers, myAnswers) = quizGame.answers.partition { it.userId == quizGame.opponentInfo.userId }
        val myCorrectAnswersCount = myAnswers.count { it.isCorrect }
        val opponentCorrectAnswersCount = opponentAnswers.count { it.isCorrect }

        gameState = when {
            opponentAnswers.size != myAnswers.size || opponentAnswers.isEmpty() -> GameState.OPEN
            myCorrectAnswersCount > opponentCorrectAnswersCount -> GameState.WON
            myCorrectAnswersCount == opponentCorrectAnswersCount -> GameState.DRAW
            else -> GameState.LOST
        }

        isOpponentsTurn = opponentAnswers.size < myAnswers.size
    }

    fun opponentAnswers() = quizGame.answers.filter {it.userId == quizGame.opponentInfo.userId}

    fun myAnswers() = quizGame.answers.filter {it.userId != quizGame.opponentInfo.userId}

    fun getAnswer(questionIndex: Int, answeredByOpponent: Boolean = false): Answer? {
        val answers = if (answeredByOpponent) opponentAnswers() else myAnswers()
        return answers.firstOrNull { it.questionIndex == questionIndex }
    }

    fun getResult(questionIndex: Int): GameState {
        val myAnswer = getAnswer(questionIndex, false)
        val opponentAnswer = getAnswer(questionIndex, true)
        return when {
            myAnswer == null || opponentAnswer == null -> GameState.OPEN
            myAnswer.answer == opponentAnswer.answer -> GameState.DRAW
            myAnswer.isCorrect && !opponentAnswer.isCorrect -> GameState.WON
            else -> GameState.LOST
        }
    }

    fun getNextQuestionIndex(): Int {
        // take the first index for which there exists no answer with that question index and my userId (!= opponentId)
        return quizGame.questions.indices.first { i -> quizGame.answers.none { answer -> answer.questionIndex == i && answer.userId != quizGame.opponentInfo.userId} }
    }

    fun getNextQuestion(): Question {
        return quizGame.questions[getNextQuestionIndex()]
    }

    companion object {
        fun from(quizGame: QuizGameDto): QuizGameViewModel {
            return QuizGameViewModel(quizGame)
        }
    }
}

class QuizGameOverviewViewModel : ViewModel() {
    var activeGames = MutableLiveData(listOf<QuizGameViewModel>())
    var finishedGames = MutableLiveData(listOf<QuizGameViewModel>())
}