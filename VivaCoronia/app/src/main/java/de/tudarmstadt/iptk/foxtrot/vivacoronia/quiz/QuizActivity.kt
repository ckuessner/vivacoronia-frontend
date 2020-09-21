package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.QuizClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ActivityQuizBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

const val ARG_QUIZ_GAME = "quiz_game_param"

class QuizActivity : AppCompatActivity() {
    private val tag = "QuizActivity"
    private lateinit var binding: ActivityQuizBinding
    lateinit var viewModel: QuizGameViewModel

    private var isNewlyCreated: Boolean = true
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quiz)

        val passedViewModel = intent.getParcelableExtra<QuizGameViewModel>(ARG_QUIZ_GAME)
        if (passedViewModel == null)
            initializeNewGame()
        else
            loadGameDetails(passedViewModel)
    }

    override fun onResume() {
        super.onResume()
        if (isNewlyCreated) {
            isNewlyCreated = false
        } else {
            fetchQuizGame()
        }
    }

    private fun fetchQuizGame() {
        GlobalScope.launch {
            try {
                val game = QuizClient.getGame(viewModel.quizGame.gameId)
                runOnUiThread { loadGameDetails(game) }
            } catch (e: Exception) {
                Log.e(tag, "Error fetching information for game ${viewModel.quizGame.gameId}: ", e)
            }
        }
    }

    private fun loadGameDetails(quizGame: QuizGameViewModel) {
        viewModel = quizGame
        val fragment = QuizDetailsFragment(this)
        supportFragmentManager.beginTransaction().replace(R.id.mainFragment, fragment).commit()
        binding.progressBar.visibility = View.GONE
    }

    private fun initializeNewGame() {
        GlobalScope.launch {
            try {
                val gameDto = QuizClient.postGame()
                db.quizGameDao().insert(QuizGame(gameDto.gameId, -1))
                val quizGame = QuizGameViewModel.from(gameDto)
                runOnUiThread { loadGameDetails(quizGame) }
            } catch (e: Exception) {
                Log.e(tag, "Error trying to initialize a new game: ", e)
            }
        }
    }

    fun onPlayButtonPressed() {
        QuizQuestionActivity.startActivity(this, viewModel.getNextQuestion(), viewModel.quizGame.gameId, viewModel.getNextQuestionIndex())
    }

    companion object {
        fun start(context: Context, quizGame: QuizGameViewModel?) {
            val intent = Intent(context, QuizActivity::class.java)
            if (quizGame != null)
                intent.putExtra(ARG_QUIZ_GAME, quizGame)
            context.startActivity(intent)
        }
    }
}