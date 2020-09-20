package de.tudarmstadt.iptk.foxtrot.vivacoronia.quiz

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class QuizGameAdapter()
    : ListAdapter<QuizGameViewModel, QuizGameOverviewViewHolder>(QuizGameDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizGameOverviewViewHolder {
        return QuizGameOverviewViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: QuizGameOverviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class QuizGameDiffCallback : DiffUtil.ItemCallback<QuizGameViewModel>() {
    override fun areItemsTheSame(oldItem: QuizGameViewModel, newItem: QuizGameViewModel): Boolean {
        return oldItem.gameId == newItem.gameId
    }

    override fun areContentsTheSame(oldItem: QuizGameViewModel, newItem: QuizGameViewModel): Boolean {
        return oldItem == newItem
    }

}

class QuizGameOverviewViewHolder(private val view: QuizGameOverviewItem) : RecyclerView.ViewHolder(view) {
    companion object {
        fun from(parent: ViewGroup): QuizGameOverviewViewHolder {
            val view = QuizGameOverviewItem(parent.context)
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            return QuizGameOverviewViewHolder(view)
        }
    }

    fun bind(item: QuizGameViewModel) {
        view.binding.quizGame = item
        if (item.finished)
            view.gameState = item.gameState // TODO check if correct
        view.binding.onClickListener = OnQuizGameItemClickListener(item)
        view.binding.executePendingBindings()
    }
}