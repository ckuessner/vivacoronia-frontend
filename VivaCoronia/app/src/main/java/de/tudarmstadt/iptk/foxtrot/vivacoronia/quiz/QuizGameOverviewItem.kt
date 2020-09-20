package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
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
    }

    fun onClick() {
        Log.d("Quiz Item", "CLICKED CLICKED CLICKED")
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

class OnQuizGameItemClickListener(private val quizGame: QuizGameViewModel) {
    fun onClick() {}
}