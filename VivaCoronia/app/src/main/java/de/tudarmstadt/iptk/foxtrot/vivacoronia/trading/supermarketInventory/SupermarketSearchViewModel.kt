package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SupermarketSearchViewModel : ViewModel() {
    val supermarkets = MutableLiveData<List<PlacesApiResult>>()
    val selectedMarker = MutableLiveData<String>()
    var errorData = MutableLiveData<PlacesApiResult>()
}

class PlacesApiResult(
    var supermarketPlaceId: String,
    var supermarketName: String,
    var supermarketLocation: LatLng)
