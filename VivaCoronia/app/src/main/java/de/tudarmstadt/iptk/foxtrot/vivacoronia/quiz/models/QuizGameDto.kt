package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models

import android.os.Parcelable
import com.beust.klaxon.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuizGameDto(
    @Json(name = "_id") val gameId: String,
    val questions: List<Question>,
    val answers: List<Answer>,
    val opponentInfo: OpponentInfo,
    val players: List<String>
) : Parcelable

@Parcelize
data class Question(
    @Json(name = "question")
    val questionText: String,
    val answers: List<String>,
    val correctAnswer: String
) : Parcelable

@Parcelize
data class Answer(
    val userId: String,
    val questionIndex: Int,
    val answer: String,
    val isCorrect: Boolean
) : Parcelable

@Parcelize
data class OpponentInfo(val userId: String, @Json(name="distance") val distanceInMeters: Float) : Parcelable