package com.github.encryptsl.lite.eco.common.extensions

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

fun convertInstant(instant: Instant): String {
   return instant
       .toJavaInstant()
       .atZone(ZoneId.systemDefault())
       .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"))
}