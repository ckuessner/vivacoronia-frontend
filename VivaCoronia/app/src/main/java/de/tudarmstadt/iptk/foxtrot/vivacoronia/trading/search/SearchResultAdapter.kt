package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.AltItemSearchResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ItemSearchResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OffersDiffCallback
import kotlin.IllegalStateException

const val OFFER_TYPE = 0
const val INVENTORY_ITEM_TYPE = 1

class SearchResultAdapter(private val clickListener: SearchResultItemListener, private val callButtonListener: SearchResultCallListener, private val switchToSupermarketListener: SearchResultSwitchToSupermarketListener) :
    ListAdapter<OfferViewModel, RecyclerView.ViewHolder>(OffersDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SearchResultViewHolder -> {
                holder.bind(getItem(position), clickListener, callButtonListener)
            }
            is AltSearchResultViewHolder -> {
                holder.bind(getItem(position), clickListener, switchToSupermarketListener)
            }
            else -> throw IllegalStateException("Invalid viewHolder found")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                SearchResultViewHolder.from(parent)
            }
            1 -> {
                AltSearchResultViewHolder.from(parent)
            }
            else -> throw IllegalStateException("Invalid viewType found")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(currentList[position].supermarketId != ""){
            INVENTORY_ITEM_TYPE
        } else OFFER_TYPE
    }
}

class SearchResultViewHolder private constructor(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): SearchResultViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemSearchResultBinding.inflate(inflater, parent, false)
            return SearchResultViewHolder(binding)
        }
    }

    fun bind(item: OfferViewModel, clickListener: SearchResultItemListener, onCallClickListener: SearchResultCallListener) {
        binding.offer = item
        binding.clickListener = clickListener
        binding.callButtonListener = onCallClickListener
        binding.executePendingBindings()
        if (item.offer.location == LatLng(0.0, 0.0)) {
            binding.viewOnMap.isEnabled = false
            binding.viewOnMap.setImageResource(R.drawable.ic_no_location)
        }
        if (item.offer.phoneNumber.isEmpty()) {
            binding.contactOwner.isEnabled = false
        }
    }
}

class AltSearchResultViewHolder private constructor(val binding: AltItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): AltSearchResultViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = AltItemSearchResultBinding.inflate(inflater, parent, false)
            return AltSearchResultViewHolder(binding)
        }
    }

    fun bind(item: OfferViewModel, clickListener: SearchResultItemListener, switchToSupermarketListener: SearchResultSwitchToSupermarketListener) {
        binding.offer = item
        binding.clickListener = clickListener
        binding.goToSupermarketListener = switchToSupermarketListener
        binding.executePendingBindings()
        if (item.offer.location == LatLng(0.0, 0.0)) {
            binding.viewOnMap.isEnabled = false
            binding.viewOnMap.setImageResource(R.drawable.ic_no_location)
        }
    }
}

class SearchResultItemListener(val clickListener: (offerId: String) -> Unit) {
    fun onViewOnMapClick(offer: OfferViewModel) = clickListener(offer.offer.id)
}

class SearchResultCallListener(val clickListener: (offerID: String) -> Unit) {
    fun onCallButtonClick(offer: OfferViewModel) = clickListener(offer.offer.id)
}

class SearchResultSwitchToSupermarketListener(val clickListener: (supermarketId: String) -> Unit){
    fun onSwitchToSupermarketButtonClick(offer: OfferViewModel) = clickListener(offer.offer.supermarketId)
}
