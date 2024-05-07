package edu.tfc.activelife.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateUtils {

    fun formatFirebaseTimestamp(seconds: Long, nanoseconds: Int): String {
        // Crea un objeto Instant usando segundos y nanosegundos
        val instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

        // Convierte el Instant a ZonedDateTime para aplicar zona horaria
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        // Formatea la fecha y hora en el formato deseado
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return formatter.format(zonedDateTime)
    }
}
