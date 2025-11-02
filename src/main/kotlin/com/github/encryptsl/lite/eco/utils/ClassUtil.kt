package com.github.encryptsl.lite.eco.utils

object ClassUtil {
    fun isValidClasspath(classpath: String): Boolean {
        return try {
            Class.forName(classpath)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}