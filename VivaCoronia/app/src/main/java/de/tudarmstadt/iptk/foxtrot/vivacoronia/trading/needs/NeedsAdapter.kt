package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.animation.collapse
import de.tudarmstadt.iptk.foxtrot.vivacoronia.animation.expand
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ListItemNeedBinding

class NeedsAdapter(
    private val deleteProductCallback: (id: String) -> Unit
) :
    ListAdapter<NeedViewModel, NeedDetailsViewHolder>(NeedDiffCallback()) {
    override fun onBindViewHolder(holder: NeedDetailsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedDetailsViewHolder {
        val holder = NeedDetailsViewHolder.from(parent, deleteProductCallback)
        holder.binding.clear.setOnClickListener {
            holder.onDeleteClick()
        }
        holder.itemView.setOnClickListener {
            holder.onExpandClick()
        }
        return holder
    }
}

class NeedDiffCallback : DiffUtil.ItemCallback<NeedViewModel>() {
    override fun areItemsTheSame(oldItem: NeedViewModel, newItem: NeedViewModel): Boolean {
        return oldItem.need.id == newItem.need.id
    }

    override fun areContentsTheSame(oldItem: NeedViewModel, newItem: NeedViewModel): Boolean {
        return oldItem.need == newItem.need
    }

}

class NeedDetailsViewHolder private constructor(
    val binding: ListItemNeedBinding,
    private val deleteProductCallback: (id: String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(
            parent: ViewGroup,
            deleteProductCallback: (id: String) -> Unit
        ): NeedDetailsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemNeedBinding.inflate(inflater, parent, false)
            return NeedDetailsViewHolder(binding, deleteProductCallback)
        }
    }

    fun onExpandClick() {
        var rotation = binding.need?.rotation
        rotation = if (rotation == 0L) 90L else 0L
        binding.expandArrow.animate().rotation(rotation.toFloat()).setDuration(200).start()
        if (binding.need!!.isExpanded)
            binding.listItemDetails.collapse()
        else
            binding.listItemDetails.expand()
        binding.need?.rotation = rotation
    }

    fun onDeleteClick(){
        deleteProductCallback(binding.need!!.baseProduct.id)
    }

    fun bind(item: NeedViewModel) {
        binding.need = item
        binding.listItemDetails.visibility = if (item.isExpanded) TableLayout.VISIBLE else TableLayout.GONE
        binding.executePendingBindings()
    }
}