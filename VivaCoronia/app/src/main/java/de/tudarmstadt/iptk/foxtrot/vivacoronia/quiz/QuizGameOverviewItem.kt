package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ItemQuizGameOverviewBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz.QuizGameViewModel.GameState

val WON_STATE = intArrayOf(R.attr.won_game)
val LOST_STATE = intArrayOf(R.attr.lost_game)
val DRAW_STATE = intArrayOf(R.attr.draw_game)
val OPEN_STATE = intArrayOf(R.attr.open_game)

class QuizGameOverviewItem : LinearLayout {
    var binding: ItemQuizGameOverviewBinding
    var gameState = GameState.OPEN
        set(value) {
            field = value
            refreshDrawableState()
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(inflater, R.layout.item_quiz_game_overview, this, true)
        colorResultBars()
    }

    fun colorResultBars() {
        val bars = arrayOf(binding.resultBar0, binding.resultBar1, binding.resultBar2, binding.resultBar3)
        for ((idx, bar) in bars.withIndex()) {
            val result = binding.quizGame?.getResult(idx) ?: return
            bar.backgroundTintList = when (result) {
                GameState.OPEN -> ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.darker_gray))
                GameState.WON -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                GameState.LOST -> ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red))
                else -> ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_orange_light))
            }

        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1) // 1 because we add 1 extra state: one gameState
        val gameState = when (gameState) {
            GameState.WON -> WON_STATE
            GameState.LOST -> LOST_STATE
            GameState.DRAW -> DRAW_STATE
            GameState.OPEN -> OPEN_STATE
        }
        mergeDrawableStates(state, gameState)
        return state
    }
}

class OnQuizGameItemClickListener(private val quizGame: QuizGameViewModel, private val context: Context) {
    fun onClick() {
        QuizActivity.start(context, quizGame)
    }
}