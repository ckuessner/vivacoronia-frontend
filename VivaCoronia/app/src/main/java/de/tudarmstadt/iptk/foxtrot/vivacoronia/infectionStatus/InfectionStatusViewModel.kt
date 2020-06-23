package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class InfectionStatusViewModel(private val unknown: String): ViewModel() {
    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
    private val formatter: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    val newStatus = MutableLiveData<String>()
    private val dateOfTest = MutableLiveData<Date?>()
    val dateOfTestString = Transformations.map(dateOfTest) {date -> dateToString(date)}
    private val occuredDateEstimation = MutableLiveData<Date?>()
    val occuredDateEstimationString = Transformations.map(occuredDateEstimation) {date -> dateToString(date)}
    val additionalInfo = MutableLiveData<JSONObject>()

    init {
        newStatus.value = unknown
        dateOfTest.value = null
        occuredDateEstimation.value = null
        additionalInfo.value = JSONObject()
    }

    fun update(data: JSONObject){
        val copy = JSONObject(data.toString())
        newStatus.value = copy.remove("newStatus") as String
        dateOfTest.value = parseDate(copy.remove("dateOfTest") as String?)
        occuredDateEstimation.value = parseDate(copy.remove("occuredDateEstimation") as String?)
        copy.remove("signature")
        additionalInfo.value = copy
    }

    private fun parseDate(text: String?): Date? {
        return if (text == null) null else parser.parse(text.replace("Z", "+0000"))
    }

    private fun dateToString(date: Date?): String {
        return (if (date != null) formatter.format(date) else null) ?: unknown
    }
}