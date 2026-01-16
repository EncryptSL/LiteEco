@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.common.hook.vault.unlocked

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.time.ExperimentalTime

class AdaptiveEconomyVaultUnlockedAPI(private val liteEco: LiteEco) : UnusedVaultUnlockedAPI() {

    companion object {
        private const val AMOUNT_APPROACHING_ZERO = "LiteEco negative amounts are not allowed."
        private const val FAIL_REACHED_BALANCE_LIMIT = "LiteEco you reach limit of balance."
        private const val SUCCESS_WITHDRAW = "LiteEco withdraw was success."
        private const val FAIL_WITHDRAW = "LiteEco something happened in withdraw process."
        private const val SUCCESS_DEPOSIT = "LiteEco deposited amount was success."
        private const val FAIL_DEPOSIT = "LiteEco something happened in deposit process."
        private const val SUCCESS_SET = "LiteEco set amount was success."
        private const val FAIL_SET = "LiteEco something happened in set process."
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name

    override fun hasMultiCurrencySupport(): Boolean = liteEco.currencyImpl.getCurrenciesKeys().size > 1

    override fun fractionalDigits(pluginName: String): Int = -1

    override fun format(pluginName: String, amount: BigDecimal): String {
        return liteEco.currencyImpl.formatted(amount)
    }

    override fun format(pluginName: String, amount: BigDecimal, currency: String): String = liteEco.currencyImpl.formatted(amount)

    override fun hasCurrency(currencyName: String): Boolean = currencies().contains(currencyName)

    override fun getDefaultCurrency(pluginName: String): String = liteEco.currencyImpl.defaultCurrency()

    override fun defaultCurrencyNamePlural(pluginName: String): String = ""

    override fun defaultCurrencyNameSingular(pluginName: String): String = ""

    override fun currencies(): MutableCollection<String> {
        return liteEco.currencyImpl.getCurrenciesKeys().toMutableList()
    }

    override fun createAccount(accountID: UUID, name: String, player: Boolean): Boolean {
        if (!player) { return false }
        val offlinePlayer = Bukkit.getOfflinePlayer(accountID)

        return runBlocking { liteEco.api.createOrUpdateAccount(offlinePlayer.uniqueId, offlinePlayer.name.toString(), liteEco.currencyImpl.defaultCurrency(), liteEco.currencyImpl.defaultStartBalance()) }
    }

    override fun createAccount(accountID: UUID, name: String, worldName: String, player: Boolean): Boolean {
        if (!player) { return false }
        val offlinePlayer = Bukkit.getOfflinePlayer(accountID)
        return runBlocking { liteEco.api.createOrUpdateAccount(offlinePlayer.uniqueId, offlinePlayer.name.toString(), liteEco.currencyImpl.defaultCurrency(), liteEco.currencyImpl.defaultStartBalance()) }
    }

    override fun deleteAccount(plugin: String, accountID: UUID): Boolean = false

    override fun getUUIDNameMap(): MutableMap<UUID, String> {
        return liteEco.api.getUUIDNameMap()
    }

    override fun getAccountName(uuid: UUID): Optional<String> {
        return runBlocking {
            liteEco.api.getUserByUUID(uuid, liteEco.currencyImpl.defaultCurrency()).getOrNull()?.let {
                Optional.of(it.userName)
            } ?: Optional.empty()
        }
    }

    override fun hasAccount(uuid: UUID): Boolean {
        return getAccountName(uuid).isPresent
    }

    override fun hasAccount(accountID: UUID, worldName: String): Boolean = hasAccount(accountID)

    override fun renameAccount(accountID: UUID, name: String): Boolean = false

    override fun renameAccount(plugin: String, accountID: UUID, name: String): Boolean = false

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String): Boolean {
        val result: Boolean = try {
            runBlocking { liteEco.api.getUserByUUID(accountID, currency).isPresent }
        } catch (_: Exception) {
            false
        }
        return result
    }

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String, world: String): Boolean {
        return accountSupportsCurrency(plugin, accountID, currency)
    }

    override fun balance(pluginName: String, accountID: UUID): BigDecimal {
        return runBlocking { liteEco.api.getBalance(accountID, liteEco.currencyImpl.defaultCurrency()) }
    }

    override fun balance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        return balance(pluginName, accountID)
    }

    override fun balance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        return runBlocking { liteEco.api.getBalance(accountID, currency) }
    }

    override fun has(pluginName: String, accountID: UUID, amount: BigDecimal): Boolean {
        return liteEco.api.has(accountID, liteEco.currencyImpl.defaultCurrency() , amount)
    }

    override fun has(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): Boolean {
        return has(pluginName, accountID, amount)
    }

    override fun has(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): Boolean {
        return liteEco.api.has(accountID, currency, amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        return withdraw(pluginName, accountID, "", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        return withdraw(pluginName, accountID, worldName, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    @OptIn(ExperimentalTime::class)
    override fun withdraw(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName try withdraw from $accountID amount $amount ($currency)")

        if (amount.isApproachingZero()) {
            return EconomyResponse(BigDecimal.ZERO, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        return runBlocking {
            try {
                if (has(pluginName, accountID, worldName, currency, amount)) {
                    val username = Bukkit.getOfflinePlayer(accountID).name ?: "Unknown"
                    val balanceBefore = liteEco.api.getBalance(accountID, currency)

                    liteEco.api.withdraw(accountID, currency, amount)

                    val balanceAfter = balanceBefore.subtract(amount)

                    liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.WITHDRAW, pluginName, username, currency, balanceBefore, balanceAfter))

                    liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName successfully withdraw $accountID. New balance: $balanceAfter")
                    EconomyResponse(amount, balanceAfter, EconomyResponse.ResponseType.SUCCESS, SUCCESS_WITHDRAW)
                } else {
                    EconomyResponse(amount, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
                }
            } catch (e: Exception) {
                liteEco.logger.error("VaultUnlocked withdraw error: ${e.message}")
                EconomyResponse(BigDecimal.ZERO, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
            }
        }
    }

    override fun deposit(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        return deposit(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        return deposit(pluginName, accountID, worldName, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    @OptIn(ExperimentalTime::class)
    override fun deposit(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName try deposit to $accountID amount $amount ($currency)")

        if (amount.isApproachingZero()) {
            return EconomyResponse(BigDecimal.ZERO, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        return runBlocking {
            try {
                if (hasAccount(accountID)) {
                    val username = Bukkit.getOfflinePlayer(accountID).name ?: "Unknown"
                    val balanceBefore = liteEco.api.getBalance(accountID, currency)

                    if (liteEco.currencyImpl.getCheckBalanceLimit(balanceBefore, currency, amount)) {
                        return@runBlocking EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
                    }

                    liteEco.api.deposit(accountID, currency, amount)

                    val balanceAfter = balanceBefore.add(amount)

                    liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.DEPOSIT, pluginName, username, currency, balanceBefore, balanceAfter))

                    liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName successfully deposit to $accountID. New balance: $balanceAfter")
                    EconomyResponse(amount, balanceAfter, EconomyResponse.ResponseType.SUCCESS, SUCCESS_DEPOSIT)
                } else {
                    EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
                }
            } catch (e: Exception) {
                liteEco.logger.error("VaultUnlocked deposit error: ${e.message}")
                EconomyResponse(BigDecimal.ZERO, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
            }
        }
    }

    override fun set(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        return set(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun set(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        return set(pluginName, accountID, worldName, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    @OptIn(ExperimentalTime::class)
    override fun set(pluginName: String, accountID: UUID, worldName: String, currency: String, amount: BigDecimal): EconomyResponse {
        liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName try set $accountID amount $amount ($currency)")

        return runBlocking {
            try {
                if (hasAccount(accountID)) {
                    val username = Bukkit.getOfflinePlayer(accountID).name ?: "Unknown"
                    val balanceBefore = liteEco.api.getBalance(accountID, currency)

                    if (liteEco.currencyImpl.getCheckBalanceLimit(amount, currency)) {
                        return@runBlocking EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
                    }

                    liteEco.api.set(accountID, currency, amount)

                    liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.SET, pluginName, username, currency, balanceBefore, amount))

                    liteEco.debugger.debug(AdaptiveEconomyVaultUnlockedAPI::class.java, "$pluginName successfully set $accountID balance to $amount")
                    EconomyResponse(amount, amount, EconomyResponse.ResponseType.SUCCESS, SUCCESS_SET)
                } else {
                    EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, FAIL_SET)
                }
            } catch (e: Exception) {
                liteEco.logger.error("VaultUnlocked set error: ${e.message}")
                EconomyResponse(BigDecimal.ZERO, balance(pluginName, accountID, worldName, currency), EconomyResponse.ResponseType.FAILURE, FAIL_SET)
            }
        }
    }
}