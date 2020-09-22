package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.FragmentQuizDetailsBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.models.Answer

class QuizDetailsFragment(private val parent: QuizActivity) : Fragment() {
    private lateinit var binding: FragmentQuizDetailsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_quiz_details, container, false)

        binding.play.setOnClickListener { parent.onPlayButtonPressed() }
        binding.play.isEnabled = !parent.viewModel.isOpponentsTurn
        binding.opponentName.text = parent.viewModel.opponentName
        initializeResults()
        binding.swipeRefresh.setOnRefreshListener { parent.fetchQuizGame() }
        return binding.root
    }

    private fun initializeResults() {
        val game = parent.viewModel
        val myAnswerViews = arrayOf(binding.myAnswer0, binding.myAnswer1, binding.myAnswer2, binding.myAnswer3)
        val opponentAnswerViews = arrayOf(binding.opponentAnswer0, binding.opponentAnswer1, binding.opponentAnswer2, binding.opponentAnswer3)

        for ((idx, answerView) in myAnswerViews.withIndex()) {
            val answer = game.getAnswer(idx, false)
            setImage(answerView, answer)
        }

        for ((idx, answerView) in opponentAnswerViews.withIndex()) {
            val answer = game.getAnswer(idx, true)
            setImage(answerView, answer)
        }

        binding.opponentCorrect.text = resources.getString(R.string.correct).format(game.opponentAnswers().filter {it.isCorrect}.size)
        binding.myCorrect.text = resources.getString(R.string.correct).format(game.myAnswers().filter {it.isCorrect}.size)
    }

    private fun setImage(answerView: ImageView, answer: Answer?) {
        when {
            answer == null -> {
                answerView.backgroundTintList = ColorStateList.valueOf(resources.getColor(android.R.color.darker_gray))
                answerView.setImageResource(R.drawable.ic_questionmark_quiz)
            }
            answer.isCorrect -> {
                answerView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.green))
                answerView.setImageResource(R.drawable.ic_checkmark_quiz)
            }
            else -> {
                answerView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
                answerView.setImageResource(R.drawable.ic_cross_quiz)
            }
        }

    }


}