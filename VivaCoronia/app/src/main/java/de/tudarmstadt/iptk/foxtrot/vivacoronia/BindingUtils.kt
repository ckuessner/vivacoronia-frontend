package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.OfferViewModel

@BindingAdapter("expandArrowRotation")
fun ImageView.setExpandArrowRotation(item: OfferViewModel) {
    rotation = item.rotation.toFloat()
}
