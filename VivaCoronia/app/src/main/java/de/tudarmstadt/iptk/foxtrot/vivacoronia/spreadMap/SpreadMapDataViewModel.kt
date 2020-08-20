package de.tudarmstadt.iptk.foxtrot.vivacoronia.spreadMap

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.ZonedDateTime

class SpreadMapDataViewModel: ViewModel() {
    val spreadMapData = MutableLiveData<MutableMap<String, List<Location>>>()
    val contactData = MutableLiveData<MutableMap<String, Pair<Boolean, ZonedDateTime>>>()
}