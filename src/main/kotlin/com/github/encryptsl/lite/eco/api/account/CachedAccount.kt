package com.github.encryptsl.lite.eco.api.account

import java.math.BigDecimal

class CachedAccount {
    val balances = mutableMapOf<String, BigDecimal>()
    var isSuccessfullyLoaded: Boolean = false
}