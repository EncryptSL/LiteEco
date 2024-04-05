package com.github.encryptsl.lite.eco.common.database.entity

import kotlinx.datetime.Instant

data class EconomyLog(val level: String, val log: String, val timestamp: Instant)
