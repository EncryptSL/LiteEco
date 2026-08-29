package com.github.encryptsl.lite.eco.api.account

import java.math.BigDecimal

class Wallet {
    var username: String = "Unknow"
    val balances = mutableMapOf<String, BigDecimal>()
    var isSuccessfullyLoaded: Boolean = false
}