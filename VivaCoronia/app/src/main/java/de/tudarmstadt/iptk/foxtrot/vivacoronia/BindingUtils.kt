package de.tudarmstadt.iptk.foxtrot.vivacoronia

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.ProductViewModel

@BindingAdapter("expandArrowRotation")
fun ImageView.setExpandArrowRotation(item: ProductViewModel) {
    rotation = item.rotation.toFloat()
}
