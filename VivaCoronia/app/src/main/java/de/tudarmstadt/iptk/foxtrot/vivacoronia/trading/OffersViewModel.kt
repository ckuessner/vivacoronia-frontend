package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

class OffersViewModel : ViewModel() {
    val offers = MutableLiveData<List<Offer>>()
}

