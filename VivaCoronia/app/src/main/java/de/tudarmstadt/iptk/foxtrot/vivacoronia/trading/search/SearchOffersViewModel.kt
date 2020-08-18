package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery


class SearchOffersViewModel : ViewModel() {
    val searchQuery = MutableLiveData<ProductSearchQuery>()
    val searchResults = MutableLiveData<List<Offer>>()

    private val _highlightOfferOnMap = MutableLiveData<Offer>()
    val highlightOfferOnMap: LiveData<Offer>
        get() = _highlightOfferOnMap

    private val _showOfferInList = MutableLiveData<Offer>()
    val showOfferInList: LiveData<Offer>
        get() = _showOfferInList

    fun onOfferDetailClick(id: String) {
        _highlightOfferOnMap.value = searchResults.value!!.first { it.id == id }
    }

    fun onCallButtonClick(id: String, activity: Activity) {
        val numberIfAvailable = searchResults.value!!.first {it.id == id}.phoneNumber
        if (numberIfAvailable != ""){
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$numberIfAvailable")
            activity.startActivity(intent)
        }
    }

    fun onOfferDetailClickNavigated() {
        _highlightOfferOnMap.value = null
    }

    fun onOfferMarkerClick(id: String) {
        _showOfferInList.value = searchResults.value!!.first { it.id == id }
    }

    fun onOfferMarkerClickNavigated() {
        _showOfferInList.value = null
    }
}