package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.AccountAPI
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap

object PlayerAccount : AccountAPI {

    private val databaseEcoModel: DatabaseEcoModel by lazy { DatabaseEcoModel() }
    private val cache: HashMap<UUID, HashMap<String, BigDecimal>> = HashMap()
    private val wallet: HashMap<String, BigDecimal> = HashMap()


    override fun cacheAccount(uuid: UUID, currency: String, value: BigDecimal) {
        if (!isAccountCached(uuid)) {
            wallet[currency] = value
            cache[uuid] = wallet
        } else {
            wallet[currency] = value
            cache[uuid] = wallet
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
            clearFromCache(uuid)
        } catch (e : Exception) {
            LiteEco.instance.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    override fun syncAccounts() {
        try {
            if (cache.isEmpty()) return
            for (entry in cache.entries) {
                for (value in entry.value) {
                    syncAccount(entry.key, value.key, value.value)
                }
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

    override fun isAccountCached(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}