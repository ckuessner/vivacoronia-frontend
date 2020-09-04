package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.ViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct

open class ProductViewModel(var baseProduct: BaseProduct) : ViewModel() {

    var product: String
        get() = baseProduct.product
        set(value){
            baseProduct.product = value
        }

    var amount: String
        get() = if (baseProduct.amount == 0) "" else baseProduct.amount.toString()
        set(value){
            baseProduct.amount = if (value == "") 0 else value.toInt()
        }

    var productCategory: String
        get() = baseProduct.productCategory
        set(value){
            baseProduct.productCategory = value
        }

    var rotation = 0L
    val isExpanded: Boolean
        get() = rotation != 0L
}