package com.github.encryptsl.lite.eco.common.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> runBlockingIO(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)