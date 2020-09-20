package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.javafaker.Faker
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.QuizGameDto
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import java.util.*


class QuizGameViewModel(
    val gameId: String,
    val opponentId: String,
    val opponentDistanceInKm: Float,
    var finishedAt: Long = 0
) : ViewModel(), Parcelable {
    enum class GameState {
        WON, LOST, DRAW, OPEN
    }

    val opponentName: String
    var gameState: GameState = GameState.OPEN // TODO set accordingly
    val finished: Boolean
        get() = gameState != GameState.OPEN

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readFloat(),
        parcel.readLong()
    ) {

    }

    init {
        val faker = Faker(Random(opponentId.hashCode().toLong()))
        val name = faker.name()
        opponentName = name.firstName() + " " + name.lastName()
    }


    override fun equals(other: Any?): Boolean {
        if (other == null || other !is QuizGameViewModel)
            return false
        return gameId == other.gameId
                && opponentId == other.opponentId
                && opponentDistanceInKm == other.opponentDistanceInKm
                && finishedAt == other.finishedAt
    }

    override fun hashCode(): Int {
        var result = gameId.hashCode()
        result = 31 * result + opponentId.hashCode()
        result = 31 * result + opponentDistanceInKm.hashCode()
        result = 31 * result + finishedAt.hashCode()
        result = 31 * result + opponentName.hashCode()
        result = 31 * result + gameState.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(gameId)
        parcel.writeString(opponentId)
        parcel.writeFloat(opponentDistanceInKm)
        parcel.writeLong(finishedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        fun from(quizGame: QuizGame): QuizGameViewModel {
            return QuizGameViewModel(quizGame.gameId, "TODO", 3f, quizGame.finishedAt)
        }

        fun from(quizGame: QuizGameDto): QuizGameViewModel {
            return QuizGameViewModel()
        }

        @Suppress("unused") // The creator is necessary to implement Parcelable
        @JvmField
        val CREATOR = object : Parcelable.Creator<QuizGameViewModel> {
            override fun createFromParcel(parcel: Parcel): QuizGameViewModel {
                return QuizGameViewModel(parcel)
            }

            override fun newArray(size: Int): Array<QuizGameViewModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}

class QuizGameOverviewViewModel : ViewModel() {
    var activeGames = MutableLiveData(listOf<QuizGameViewModel>())
    var finishedGames = MutableLiveData(listOf<QuizGameViewModel>())
}