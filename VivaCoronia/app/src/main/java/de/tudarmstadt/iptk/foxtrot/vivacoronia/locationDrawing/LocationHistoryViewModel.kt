package de.tudarmstadt.iptk.foxtrot.vivacoronia.locationDrawing

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationHistoryViewModel: ViewModel() {
    val locationHistory = MutableLiveData<ArrayList<Location>>()
}