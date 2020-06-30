package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.animation.collapse
import de.tudarmstadt.iptk.foxtrot.vivacoronia.animation.expand
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ListItemOfferBinding

class OffersAdapter(private val deleteOfferCallback: (id: String) -> Unit
) :
    ListAdapter<OfferViewModel, OfferDetailsViewHolder>(OffersDiffCallback()) {
    override fun onBindViewHolder(holder: OfferDetailsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferDetailsViewHolder {
        val holder = OfferDetailsViewHolder.from(parent, deleteOfferCallback)
        holder.binding.clear.setOnClickListener {
            holder.onDeleteClick()
        }
        holder.binding.editProduct.setOnClickListener {
            holder.onEditClick()
        }
        holder.itemView.setOnClickListener {// TODO move listeners out of here
            holder.onExpandClick()
        }
        return holder
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
    val binding: ListItemOfferBinding,
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

    fun onExpandClick() {
        var rotation = binding.offer?.rotation
        rotation = if (rotation == 0L) 90L else 0L
        binding.expandArrow.animate().rotation(rotation.toFloat()).setDuration(200).start()
        if (binding.offer!!.isExpanded)
            binding.listItemDetails.collapse()
        else
            binding.listItemDetails.expand() // TODO scroll to bottom if last element ?
        binding.offer?.rotation = rotation
    }

    fun onDeleteClick(){
        deleteOfferCallback(binding.offer!!.offer.id)
    }

    fun onEditClick() {
        // TODO
        Log.d("OFFER-VIEW-HOLDER", "Clicked edit")
    }

    fun bind(item: OfferViewModel) {
        binding.offer = item
        binding.listItemDetails.visibility = if (item.isExpanded) TableLayout.VISIBLE else TableLayout.GONE
        binding.executePendingBindings()
    }
}
