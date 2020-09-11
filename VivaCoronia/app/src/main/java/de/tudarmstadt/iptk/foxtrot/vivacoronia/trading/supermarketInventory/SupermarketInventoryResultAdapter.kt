package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.SupermarketInventoryItemBinding

class SupermarketInventoryResultAdapter(private val clickListener: SupermarketItemListener) : ListAdapter<SupermarketInventoryItemViewModel, SupermarketInventoryResultViewHolder>(DiffCallback()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SupermarketInventoryResultViewHolder {
        return SupermarketInventoryResultViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SupermarketInventoryResultViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

}

class DiffCallback: DiffUtil.ItemCallback<SupermarketInventoryItemViewModel>() {
    override fun areItemsTheSame(
        oldItem: SupermarketInventoryItemViewModel,
        newItem: SupermarketInventoryItemViewModel
    ): Boolean {
        return oldItem.inventoryItem.id == newItem.inventoryItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: SupermarketInventoryItemViewModel,
        newItem: SupermarketInventoryItemViewModel
    ): Boolean {
        return oldItem.inventoryItem == newItem.inventoryItem
    }

}

class SupermarketInventoryResultViewHolder private constructor(val binding: SupermarketInventoryItemBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): SupermarketInventoryResultViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = SupermarketInventoryItemBinding.inflate(inflater, parent, false)
            return SupermarketInventoryResultViewHolder(binding)
        }
    }

    fun bind(
        item: SupermarketInventoryItemViewModel,
        clickListener: SupermarketItemListener
    ) {
        binding.inventoryItem = item
        binding.clickListener = clickListener
        val ids = arrayListOf<Int>()
        for(child in binding.availabilityChoices.children){
            ids.add(child.id)
            if(child.findViewById<MaterialButton>(R.id.unavailable) != null) child.findViewById<MaterialButton>(R.id.unavailable).setIconTintResource(R.color.grey)
            if(child.findViewById<MaterialButton>(R.id.low) != null) child.findViewById<MaterialButton>(R.id.low).setIconTintResource(R.color.grey)
            if(child.findViewById<MaterialButton>(R.id.medium) != null) child.findViewById<MaterialButton>(R.id.medium).setIconTintResource(R.color.grey)
            if(child.findViewById<MaterialButton>(R.id.high) != null) child.findViewById<MaterialButton>(R.id.high).setIconTintResource(R.color.grey)
        }
        when(item.inventoryItem.availability){
            0 -> {
                binding.availabilityChoices.check(ids[0])
                binding.availabilityChoices.getChildAt(0).findViewById<MaterialButton>(R.id.unavailable).iconTint = null
            }
            1 -> {
                binding.availabilityChoices.check(ids[1])
                binding.availabilityChoices.getChildAt(1).findViewById<MaterialButton>(R.id.low).iconTint = null
            }
            2 -> {
                binding.availabilityChoices.check(ids[2])
                binding.availabilityChoices.getChildAt(2).findViewById<MaterialButton>(R.id.medium).iconTint = null
            }
            else -> {
                binding.availabilityChoices.check(ids[3])
                binding.availabilityChoices.getChildAt(3).findViewById<MaterialButton>(R.id.high).iconTint = null
            }
        }
        binding.executePendingBindings()
    }
}

class SupermarketItemListener(val clickListener: (supermarketItem: SupermarketInventoryItemViewModel, availability: Int) -> Unit){
    fun onEditButtonClick(supermarketItem: SupermarketInventoryItemViewModel, availability: Int) = clickListener(supermarketItem, availability)
}