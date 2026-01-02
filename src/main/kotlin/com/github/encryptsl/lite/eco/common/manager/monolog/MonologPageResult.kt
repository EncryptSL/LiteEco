package com.github.encryptsl.lite.eco.common.manager.monolog

import net.kyori.adventure.text.Component

data class MonologPageResult(
    val components: List<Component>,
    val totalPages: Int
)