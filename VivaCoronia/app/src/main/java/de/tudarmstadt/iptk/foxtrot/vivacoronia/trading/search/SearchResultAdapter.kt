package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.databinding.ItemSearchResultBinding
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OffersDiffCallback

class SearchResultAdapter(private val clickListener: SearchResultItemListener, private val callButtonListener: SearchResultCallListener) :
    ListAdapter<OfferViewModel, SearchResultViewHolder>(OffersDiffCallback()) {

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, callButtonListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder.from(parent)
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

class SearchResultItemListener(val clickListener: (offerId: String) -> Unit) {
    fun onViewOnMapClick(offer: OfferViewModel) = clickListener(offer.offer.id)
}

class SearchResultCallListener(val clickListener: (offerID: String) -> Unit) {
    fun onCallButtonClick(offer: OfferViewModel) = clickListener(offer.offer.id)
}
