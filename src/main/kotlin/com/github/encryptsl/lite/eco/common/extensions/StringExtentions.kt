package com.github.encryptsl.lite.eco.common.extensions

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun convertInstant(instant: Instant): String {
   return instant
       .toJavaInstant()
       .atZone(ZoneId.systemDefault())
       .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"))
}