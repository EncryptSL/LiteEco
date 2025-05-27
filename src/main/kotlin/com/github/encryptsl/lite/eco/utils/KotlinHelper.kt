package com.github.encryptsl.lite.eco.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> runBlockingIO(crossinline block: () -> T): T =
    withContext(Dispatchers.IO) { block() }