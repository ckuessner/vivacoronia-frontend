package de.tudarmstadt.iptk.foxtrot.vivacoronia.utils

import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    val sdf = SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.GERMANY)

    fun toRFCString(date: Date): String = sdf.format(date)
}