package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("expandArrowRotation")
fun ImageView.setExpandArrowRotation(item: OfferViewModel) {
    rotation = item.rotation.toFloat()
}
