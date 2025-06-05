package com.github.encryptsl.lite.eco.common.manager.importer

import java.math.BigDecimal

data class EconomyImportResults(
    val converted: Int,
    val balances: BigDecimal
)