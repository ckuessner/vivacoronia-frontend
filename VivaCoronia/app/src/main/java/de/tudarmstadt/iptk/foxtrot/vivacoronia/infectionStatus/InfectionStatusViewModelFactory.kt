package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InfectionStatusViewModelFactory(private val unknown: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfectionStatusViewModel::class.java)) {
            return InfectionStatusViewModel(unknown) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class for InfectionStatusViewModel")
    }

}