package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.OfferViewModel

@BindingAdapter("expandArrowRotation")
fun ImageView.setExpandArrowRotation(item: OfferViewModel) {
    rotation = item.rotation.toFloat()
}
