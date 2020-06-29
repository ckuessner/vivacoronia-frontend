package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

class OffersAdapter : RecyclerView.Adapter<OfferDetailsViewHolder>(){
    var data = listOf<Offer>()
        set(value) {
            field = value
            rotations = MutableList(value.size) { 0L }
            notifyDataSetChanged()
        }

    var rotations = mutableListOf<Long>()

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: OfferDetailsViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            rotations[position] = holder.handleClick(200)
        }
        val item = data[position]
        val rotation = rotations[position]
        holder.bind(item, rotation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.offer_item_view, parent, false) as View
        return OfferDetailsViewHolder(view)
    }
}

class OfferDetailsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var rotationAngle = 0L
    private val expandArrow: ImageView = itemView.findViewById(R.id.expand_arrow)
    private val productName: TextView = itemView.findViewById(R.id.product_name)

    fun handleClick(animationSpeed: Long): Long{
        rotationAngle = if (rotationAngle == 0L) 90L else 0L
        expandArrow.animate().rotation(rotationAngle.toFloat()).setDuration(animationSpeed).start()
        return rotationAngle
    }

    fun bind(item: Offer, rotation: Long){
        rotationAngle = rotation
        expandArrow.rotation = rotation.toFloat()
        productName.text = item.productName
    }
}
