package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class QuizGameAdapter(private val context: Context)
    : ListAdapter<QuizGameViewModel, QuizGameOverviewViewHolder>(QuizGameDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizGameOverviewViewHolder {
        return QuizGameOverviewViewHolder.from(parent, context)
    }

    override fun onBindViewHolder(holder: QuizGameOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class QuizGameDiffCallback : DiffUtil.ItemCallback<QuizGameViewModel>() {
    override fun areItemsTheSame(oldItem: QuizGameViewModel, newItem: QuizGameViewModel): Boolean {
        return oldItem.quizGame.gameId == newItem.quizGame.gameId
    }

    override fun areContentsTheSame(oldItem: QuizGameViewModel, newItem: QuizGameViewModel): Boolean {
        return oldItem.quizGame == newItem.quizGame
    }

}

class QuizGameOverviewViewHolder(private val view: QuizGameOverviewItem, private val context: Context) : RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup, context: Context): QuizGameOverviewViewHolder {
            val view = QuizGameOverviewItem(parent.context)
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            return QuizGameOverviewViewHolder(view, context)
        }
    }

    fun bind(item: QuizGameViewModel) {
        view.binding.quizGame = item
        view.gameState = item.gameState
        view.binding.onClickListener = OnQuizGameItemClickListener(item, context)
        view.binding.executePendingBindings()
        view.colorResultBars()
    }
}