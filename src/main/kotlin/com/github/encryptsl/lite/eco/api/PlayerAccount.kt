package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    private val cache = mutableMapOf<UUID, MutableMap<String, BigDecimal>>()

    override fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal) {
        if (!isAccountCached(uuid, currency)) {
            cache.computeIfAbsent(uuid) { mutableMapOf() } [currency] = value
        } else {
            cache[uuid]?.computeIfPresent(currency) { _, _ -> value }
        }
    }

    override fun getBalance(uuid: UUID, currency: String): BigDecimal {
        return cache[uuid]?.getOrDefault(currency, BigDecimal.ZERO) ?: BigDecimal.ZERO
    }

    override fun syncAccount(uuid: UUID) {
        val userBalances = cache[uuid] ?: return
        try {
            userBalances.forEach { (currency, amount) ->
                val finalAmount = if (LiteEco.instance.currencyImpl.getCheckBalanceLimit(amount, currency)) {
                    LiteEco.instance.currencyImpl.getCurrencyLimit(currency) // set maximum from configuration
                } else {
                    amount
                }
                databaseEcoModel.set(uuid, currency, finalAmount)
            }
            cache.remove(uuid)
        } catch (e: Exception) {
            LiteEco.instance.logger.severe("Error while sync cache with database for $uuid: ${e.localizedMessage}")
        }
    }

    override fun syncAccounts() {
        try {
            cache.entries.forEach { user -> user.value.forEach {
                val finalAmount = if (LiteEco.instance.currencyImpl.getCheckBalanceLimit(it.value, it.key)) {
                    LiteEco.instance.currencyImpl.getCurrencyLimit(it.key)
                } else {
                    it.value
                }
                databaseEcoModel.set(user.key, it.key, finalAmount) }
            }
            cache.clear()
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun clearFromCache(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID, currency: String?): Boolean {
       return cache.containsKey(uuid) && cache[uuid]?.containsKey(currency) == true
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}