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

    override fun syncAccount(uuid: UUID, currency: String, value: BigDecimal) {
        try {
            databaseEcoModel.setMoney(uuid, currency, value)
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccount(uuid: UUID) {
        try {
            val iterator = cache[uuid]?.iterator()
            while (iterator?.hasNext() == true) {
                val balance = iterator.next()
                databaseEcoModel.setMoney(uuid, balance.key, balance.value)
            }
            cache.remove(uuid)
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccounts() {
        try {
            cache.entries.forEach { user -> user.value.forEach {
                databaseEcoModel.setMoney(user.key, it.key, it.value) }
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