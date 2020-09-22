package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.QuizGameApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ActivityQuizQuestionBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Question
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val ARG_QUESTION = "question"
const val ARG_GAME_ID = "game_id"
const val ARG_GAME_FINISHED = "game_finished"
const val ARG_QUESTION_INDEX = "question_index"

class QuizQuestionActivity : AppCompatActivity() {
    private lateinit var gameId: String
    private var questionIndex: Int = 0
    private lateinit var question: Question
    private lateinit var binding: ActivityQuizQuestionBinding
    private var isAnswered = false
    private val tag = "QuizQuestionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        question = intent.getParcelableExtra(ARG_QUESTION)!!
        gameId = intent.getStringExtra(ARG_GAME_ID)!!
        questionIndex = intent.getIntExtra(ARG_QUESTION_INDEX, 0)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quiz_question)
        binding.question = question
        binding.answer0.setOnClickListener { onAnswerClicked(0) }
        binding.answer1.setOnClickListener { onAnswerClicked(1) }
        binding.answer2.setOnClickListener { onAnswerClicked(2) }
        binding.answer3.setOnClickListener { onAnswerClicked(3) }
    }

    private fun onAnswerClicked(answerIndex: Int) {
        if (isAnswered)
            return
        val answer = question.answers[answerIndex]
        GlobalScope.launch {
            try {
                QuizGameApiClient.postGameAnswer(this@QuizQuestionActivity, gameId, questionIndex, answer)
            } catch (e: Exception) {
                Log.i(tag, "Failed to post answer:", e)
            }
        }
        colorAnswer(answerIndex, answer == question.correctAnswer)
        isAnswered = true
        Handler().postDelayed({ finish() }, 1500)
    }

    private fun colorAnswer(answerIndex: Int, isCorrect: Boolean) {
        val color = if (isCorrect) R.color.green else R.color.red
        val button = when (answerIndex) {
            0 -> binding.answer0
            1 -> binding.answer1
            2 -> binding.answer2
            else -> binding.answer3
        }
        button.backgroundTintList = ColorStateList.valueOf(resources.getColor(color))
    }

    companion object {
        @JvmStatic
        fun startActivity(context: Context, question: Question, gameId: String, questionIndex: Int) {
            val intent = Intent(context, QuizQuestionActivity::class.java)
            intent.putExtra(ARG_QUESTION, question)
            intent.putExtra(ARG_GAME_ID, gameId)
            intent.putExtra(ARG_QUESTION_INDEX, questionIndex)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}