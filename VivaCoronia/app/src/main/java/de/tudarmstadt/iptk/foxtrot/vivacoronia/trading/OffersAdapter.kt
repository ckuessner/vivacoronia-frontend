package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ListItemOfferBinding

class OffersAdapter(private val deleteOfferCallback: (id: String) -> Unit
) :
    ListAdapter<OfferViewModel, OfferDetailsViewHolder>(OffersDiffCallback()) {
    override fun onBindViewHolder(holder: OfferDetailsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferDetailsViewHolder {
        return OfferDetailsViewHolder.from(parent, deleteOfferCallback)
    }
}

class OffersDiffCallback : DiffUtil.ItemCallback<OfferViewModel>() {
    override fun areItemsTheSame(oldItem: OfferViewModel, newItem: OfferViewModel): Boolean {
        return oldItem.offer.id == newItem.offer.id
    }

    override fun areContentsTheSame(oldItem: OfferViewModel, newItem: OfferViewModel): Boolean {
        return oldItem.offer == newItem.offer
    }

}

class OfferDetailsViewHolder private constructor(
    private val binding: ListItemOfferBinding,
    private val deleteOfferCallback: (id: String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(
            parent: ViewGroup,
            deleteOfferCallback: (id: String) -> Unit
        ): OfferDetailsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemOfferBinding.inflate(inflater, parent, false)
            return OfferDetailsViewHolder(binding, deleteOfferCallback)
        }
    }

    private fun onExpandClick() {
        var rotation = binding.offer?.rotation
        rotation = if (rotation == 0L) 90L else 0L
        binding.expandArrow.animate().rotation(rotation.toFloat()).setDuration(200).start()
        binding.offer?.rotation = rotation
    }

    fun bind(item: OfferViewModel) {
        binding.offer = item
        binding.clear.setOnClickListener {
            deleteOfferCallback(binding.offer!!.offer.id)
        }
        itemView.setOnClickListener {
            onExpandClick()
        }
        binding.executePendingBindings()
    }
}
