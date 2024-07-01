package com.github.encryptsl.lite.eco.common.hook.vaultunlocked

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class AdaptiveEconomyVaultUnlockedAPI(private val liteEco: LiteEco) : UnusedVaultUnlockedAPI() {

    companion object {
        private const val MULTI_CURRENCIES_NOT_SUPPORTED_MESSAGE = "LiteEco does not support multi currencies, actions !"
        private const val MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE = "LiteEco does not support multi world currencies, actions !"
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name


    override fun hasBankSupport(): Boolean = false

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(): Int { return 2 }

    override fun format(value: BigDecimal): String {
        return liteEco.api.formatted(value.toDouble())
    }

    override fun format(value: BigDecimal, p1: String?): String {
        return liteEco.api.formatted(value.toDouble())
    }

    override fun hasCurrency(p0: String?): Boolean = false

    override fun getDefaultCurrency(): String {
        return "dollars"
    }

    override fun defaultCurrencyNamePlural(): String {
        return "dollars"
    }

    override fun defaultCurrencyNameSingular(): String {
        return "dollar"
    }

    override fun currencies(): MutableCollection<String> {
        return mutableSetOf("dollars")
    }

    override fun createAccount(uuid: UUID?, p1: String?): Boolean {
        if (uuid == null) { return false }

        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), liteEco.config.getDouble("economy.starting_balance"))
    }

    override fun createAccount(uuid: UUID?, p1: String?, p2: String?): Boolean {
        if (uuid == null) return false

        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), liteEco.config.getDouble("economy.starting_balance"))
    }

    override fun getUUIDNameMap(): MutableMap<UUID, String> {
        return emptyMap<UUID, String>().toMutableMap()
    }

    override fun getAccountName(p0: UUID?): Optional<String> {
        TODO("Not yet implemented")
    }

    override fun hasAccount(uuid: UUID): Boolean {
        return liteEco.api.getUserByUUID(Bukkit.getOfflinePlayer(uuid)).thenApply {
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.get()
    }

    override fun hasAccount(uuid: UUID, p1: String?): Boolean {
        return hasAccount(uuid)
    }

    override fun renameAccount(p0: UUID?, p1: String?): Boolean { return false }

    override fun getBalance(pluginName: String?, uuid: UUID?): BigDecimal {
        if (uuid == null) return BigDecimal.ZERO

        return liteEco.api.getBalance(Bukkit.getOfflinePlayer(uuid)).toBigDecimal()
    }

    override fun getBalance(pluginName: String?, uuid: UUID?, p2: String?): BigDecimal {
        return getBalance(null, uuid)
    }

    override fun getBalance(pluginName: String?, uuid: UUID?, p2: String?, p3: String?): BigDecimal {
        return getBalance(null, uuid)
    }

    override fun has(pluginName: String, uuid: UUID?, value: BigDecimal?): Boolean {
        if (uuid == null || value == null) return false

        return liteEco.api.has(Bukkit.getOfflinePlayer(uuid), value.toDouble())
    }

    override fun has(pluginName: String, uuid: UUID?, currency: String?, value: BigDecimal?): Boolean {
        return has(pluginName, uuid, value)
    }

    override fun has(pluginName: String, uuid: UUID?, worldName: String?, currency: String?, value: BigDecimal?): Boolean {
        return has(pluginName, uuid, value)
    }

    override fun withdraw(pluginName: String, uuid: UUID?, amount: BigDecimal?): EconomyResponse {
        if (amount == null) {
            return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
        }

        if (uuid == null || amount.toDouble().isApproachingZero()) {
            return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
        }

        val player = Bukkit.getOfflinePlayer(uuid)

        return if (has(pluginName, uuid, amount)) {
            liteEco.api.withDrawMoney(player, amount.toDouble())
            EconomyResponse(amount, getBalance(null, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(amount, getBalance(null, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        }
    }

    override fun withdraw(pluginName: String, uuid: UUID?, currency: String?, amount: BigDecimal?): EconomyResponse {
        if (currency != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return withdraw(pluginName, uuid, amount)
    }

    override fun withdraw(pluginName: String, uuid: UUID?, worldName: String?, currency: String?, amount: BigDecimal?): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        if (currency != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return withdraw(pluginName, uuid, amount)
    }

    override fun deposit(pluginName: String, uuid: UUID?, amount: BigDecimal): EconomyResponse {
        if (uuid == null || amount.toDouble().isApproachingZero()) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, null)
        }

        val player = Bukkit.getOfflinePlayer(uuid)

        return if (has(pluginName, player.uniqueId, amount)) {
            liteEco.api.withDrawMoney(player, amount.toDouble())
            EconomyResponse(amount, getBalance(pluginName, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(amount, getBalance(pluginName, player.uniqueId), EconomyResponse.ResponseType.FAILURE, null)
        }
    }

    override fun deposit(pluginName: String, uuid: UUID?, currency: String?, amount: BigDecimal): EconomyResponse {
        if (currency != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return deposit(pluginName, uuid, amount)
    }

    override fun deposit(pluginName: String, uuid: UUID?, worldName: String?, currency: String?, amount: BigDecimal): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        if (currency != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return deposit(pluginName, uuid, amount)
    }
}