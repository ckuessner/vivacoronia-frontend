package de.tudarmstadt.iptk.foxtrot.vivacoronia.spreadMap

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SpreadMapDataViewModel: ViewModel() {
    val spreadMapData = MutableLiveData<MutableMap<Int, List<Location>>>()
}