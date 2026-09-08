package com.github.encryptsl.lite.eco.api.account

import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class Wallet {
    var username: String = "Unknow"
    val balances: ConcurrentHashMap<String, BigDecimal> = ConcurrentHashMap()
    var isSuccessfullyLoaded: Boolean = false
}