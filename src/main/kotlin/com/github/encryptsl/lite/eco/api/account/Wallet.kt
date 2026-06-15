package com.github.encryptsl.lite.eco.api.account

import java.math.BigDecimal

class Wallet {
    val balances = mutableMapOf<String, BigDecimal>()
    var isSuccessfullyLoaded: Boolean = false
}