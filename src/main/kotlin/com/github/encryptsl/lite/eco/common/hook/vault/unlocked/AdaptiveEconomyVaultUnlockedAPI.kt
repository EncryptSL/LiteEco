package com.github.encryptsl.lite.eco.common.hook.vault.unlocked

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class AdaptiveEconomyVaultUnlockedAPI(private val liteEco: LiteEco) : UnusedVaultUnlockedAPI() {

    companion object {
        private const val MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE = "LiteEco does not support multi world currencies, actions !"
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name


    override fun hasBankSupport(): Boolean = false

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(): Int = -1

    override fun format(value: BigDecimal): String {
        return liteEco.api.formatted(value)
    }

    override fun format(value: BigDecimal, p1: String?): String {
        return liteEco.api.formatted(value)
    }

    override fun hasCurrency(currencyName: String): Boolean = false

    override fun getDefaultCurrency(): String = liteEco.currencyImpl.defaultCurrency()

    override fun defaultCurrencyNamePlural(): String = ""

    override fun defaultCurrencyNameSingular(): String = ""

    override fun currencies(): MutableCollection<String> {
        return liteEco.currencyImpl.getCurrenciesKeys().toMutableList()
    }

    override fun createAccount(uuid: UUID?, p1: String?): Boolean {
        if (uuid == null) { return false }

        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), startAmount = liteEco.currencyImpl.defaultStartBalance())
    }

    override fun createAccount(uuid: UUID?, p1: String?, p2: String?): Boolean {
        if (uuid == null) return false

        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(uuid), startAmount = liteEco.currencyImpl.defaultStartBalance())
    }

    override fun getUUIDNameMap(): MutableMap<UUID, String> {
        return liteEco.api.getUUIDNameMap()
    }

    override fun getAccountName(uuid: UUID): Optional<String> {
        val username = liteEco.api.getUserByUUID(Bukkit.getOfflinePlayer(uuid), liteEco.currencyImpl.defaultCurrency()).join().userName
        return Optional.of(username)
    }

    override fun hasAccount(uuid: UUID): Boolean {
        return liteEco.api.getUserByUUID(Bukkit.getOfflinePlayer(uuid), liteEco.currencyImpl.defaultCurrency()).thenApply {
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

        return liteEco.api.getBalance(Bukkit.getOfflinePlayer(uuid), liteEco.currencyImpl.defaultCurrency())
    }

    override fun getBalance(pluginName: String?, uuid: UUID?, worldName: String?): BigDecimal {
        return getBalance(null, uuid)
    }

    override fun getBalance(pluginName: String?, uuid: UUID?, p2: String?, currency: String?): BigDecimal {
        if (uuid == null || currency == null) return BigDecimal.ZERO

        return liteEco.api.getBalance(Bukkit.getOfflinePlayer(uuid), currency)
    }

    override fun has(pluginName: String?, uuid: UUID?, value: BigDecimal?): Boolean {
        if (uuid == null || value == null) return false

        return liteEco.api.has(Bukkit.getOfflinePlayer(uuid), liteEco.currencyImpl.defaultCurrency() , value)
    }

    override fun has(pluginName: String?, uuid: UUID?, worldName: String?, value: BigDecimal?): Boolean {
        return has(pluginName, uuid, value)
    }

    override fun has(pluginName: String?, uuid: UUID?, worldName: String?, currency: String?, value: BigDecimal?): Boolean {
        if (uuid == null || currency == null || value == null) return false

        return liteEco.api.has(Bukkit.getOfflinePlayer(uuid), currency , value)
    }

    override fun withdraw(pluginName: String?, uuid: UUID?, amount: BigDecimal?): EconomyResponse {
        if (amount == null) {
            return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
        }

        if (uuid == null || amount.isApproachingZero()) {
            return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
        }

        val player = Bukkit.getOfflinePlayer(uuid)

        return if (has(pluginName, uuid, amount)) {
            liteEco.api.withDrawMoney(player, liteEco.currencyImpl.defaultCurrency(), amount)
            EconomyResponse(amount, getBalance(null, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(amount, getBalance(null, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        }
    }

    override fun withdraw(pluginName: String?, uuid: UUID?, worldName: String?, amount: BigDecimal?): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return withdraw(pluginName, uuid, amount)
    }

    override fun withdraw(pluginName: String?, uuid: UUID?, worldName: String?, currency: String?, amount: BigDecimal?): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        if (currency != null) {
            if (amount == null) {
                return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
            }

            if (uuid == null || amount.isApproachingZero()) {
                return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, null)
            }

            val player = Bukkit.getOfflinePlayer(uuid)

            return if (has(pluginName, uuid, amount)) {
                liteEco.api.withDrawMoney(player, currency, amount)
                EconomyResponse(amount, getBalance(pluginName, player.uniqueId, null, currency), EconomyResponse.ResponseType.SUCCESS, null)
            } else {
                EconomyResponse(amount, getBalance(pluginName, player.uniqueId, null, currency), EconomyResponse.ResponseType.SUCCESS, null)
            }
        }

        return withdraw(pluginName, uuid, amount)
    }

    override fun deposit(pluginName: String?, uuid: UUID?, amount: BigDecimal): EconomyResponse {
        if (uuid == null || amount.isApproachingZero()) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, null)
        }

        val player = Bukkit.getOfflinePlayer(uuid)

        return if (has(pluginName, player.uniqueId, amount)) {
            liteEco.api.withDrawMoney(player, liteEco.currencyImpl.defaultCurrency(), amount)
            EconomyResponse(amount, getBalance(pluginName, player.uniqueId), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(amount, getBalance(pluginName, player.uniqueId), EconomyResponse.ResponseType.FAILURE, null)
        }
    }

    override fun deposit(pluginName: String?, uuid: UUID?, worldName: String?, amount: BigDecimal): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        return deposit(pluginName, uuid, amount)
    }

    override fun deposit(pluginName: String?, uuid: UUID?, worldName: String?, currency: String?, amount: BigDecimal): EconomyResponse {
        if (worldName != null) {
            return EconomyResponse(amount, getBalance(pluginName, uuid), EconomyResponse.ResponseType.FAILURE, MULTI_WORLD_CURRENCIES_NOT_SUPPORTED_MESSAGE)
        }

        if (currency != null) {
            if (uuid == null || amount.isApproachingZero()) {
                return EconomyResponse(amount, getBalance(pluginName, uuid, null, currency), EconomyResponse.ResponseType.FAILURE, null)
            }

            val player = Bukkit.getOfflinePlayer(uuid)

            return if (has(pluginName, player.uniqueId, amount)) {
                liteEco.api.withDrawMoney(player, currency, amount)
                EconomyResponse(amount, getBalance(pluginName, player.uniqueId, null, currency), EconomyResponse.ResponseType.SUCCESS, null)
            } else {
                EconomyResponse(amount, getBalance(pluginName, player.uniqueId, null, currency), EconomyResponse.ResponseType.FAILURE, null)
            }
        }

        return deposit(pluginName, uuid, amount)
    }
}