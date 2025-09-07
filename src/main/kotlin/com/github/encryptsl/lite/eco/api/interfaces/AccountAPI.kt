package com.github.encryptsl.lite.eco.api.interfaces

import java.math.BigDecimal
import java.util.*

interface AccountAPI {
    fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal)
    fun syncAccount(uuid: UUID)
    fun syncAccounts()
    fun clearFromCache(uuid: UUID)
    fun getBalance(uuid: UUID, currency: String): BigDecimal
    fun isAccountCached(uuid: UUID, currency: String?): Boolean
    fun isPlayerOnline(uuid: UUID): Boolean
}