package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ListItemNeedBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.NeedViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.ProductViewModel

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

    fun onDeleteClick(){
        deleteProductCallback(binding.need!!.baseProduct.id)
    }

    fun bind(item: ProductViewModel) {
        binding.need = item
        binding.executePendingBindings()
    }
}