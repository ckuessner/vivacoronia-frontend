package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.ProductViewModel
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need
import java.lang.IllegalArgumentException

class NeedListViewModel : ViewModel(){
    val needsList = MutableLiveData<MutableList<NeedViewModel>>()

    fun setNeeds(list: List<Need>) {
        needsList.value = MutableList(list.size) { index ->
            NeedViewModel(
                list[index]
            )
        }
    }

    fun add(list: List<Need>) {
        val value = this.needsList.value ?: mutableListOf()
        value.addAll(List(list.size) { index ->
            NeedViewModel(
                list[index]
            )
        })
        needsList.value = value
    }
}

class NeedViewModel(var need: Need) : ProductViewModel(need as BaseProduct)

class NeedViewModelFactory(private val need: Need): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NeedViewModel::class.java)) {
            return NeedViewModel(need) as T
        }
        throw IllegalArgumentException("Unknown NeedViewModel class")
    }

}