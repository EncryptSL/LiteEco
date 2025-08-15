package com.github.encryptsl.lite.eco.common.extensions

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

@OptIn(ExperimentalTime::class)
fun convertInstant(instant: Instant): String {
   return instant
       .toJavaInstant()
       .atZone(ZoneId.systemDefault())
       .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"))
}