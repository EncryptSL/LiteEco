@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.common.hook.vault.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer

@Suppress("DEPRECATION")
class LegacyEconomyVaultAPI(private val liteEco: LiteEco) : LegacyDeprecatedEconomy() {

    companion object {
        private const val BANK_NOT_SUPPORTED_MESSAGE = "LiteEco does not support bank accounts!"
        private const val FAIL_REACHED_BALANCE_LIMIT = "LiteEco: account limit exceeded."
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name

    override fun hasBankSupport(): Boolean = false

    override fun fractionalDigits(): Int = -1

    override fun format(amount: Double): String = liteEco.currencyImpl.fullFormatting(amount.toBigDecimal())

    override fun currencyNamePlural(): String? = null

    override fun currencyNameSingular(): String? = null

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return liteEco.api.account().hasAccount(player.uniqueId)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String?): Boolean {
        return hasAccount(player)
    }

    override fun getBalance(player: OfflinePlayer?): Double {
        if (player == null) return 0.0

        return try {
            runBlocking {
                liteEco.api.account().getBalance(player.uniqueId, liteEco.currencyImpl.defaultCurrency()).toDouble()
            }
        } catch (e: Exception) {
            liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "Error getting balance for ${player.name}: ${e.message}")
            0.0
        }
    }

    override fun getBalance(player: OfflinePlayer, world: String?): Double {
        return this.getBalance(player)
    }

    override fun has(player: OfflinePlayer?, amount: Double): Boolean {
        if (player == null || amount < 0) return false
        val defaultCurrency = liteEco.currencyImpl.defaultCurrency()
        return liteEco.api.account().has(player.uniqueId, defaultCurrency, amount.toBigDecimal())
    }

    override fun has(player: OfflinePlayer?, worldName: String?, amount: Double): Boolean {
        return has(player, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null || amount <= 0.0) {
            val currentBalance = if (player != null) getBalance(player) else 0.0
            return EconomyResponse(0.0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Invalid amount or player")
        }

        val currency = liteEco.currencyImpl.defaultCurrency()
        val amountBd = amount.toBigDecimal()

        return try {
            runBlocking {
                if (!liteEco.api.account().has(player.uniqueId, currency, amountBd)) {
                    val currentBalance = liteEco.api.account().getBalance(player.uniqueId, currency).toDouble()
                    return@runBlocking EconomyResponse(0.0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds")
                }

                liteEco.api.account().withdraw(player.uniqueId, currency, amountBd)
                val newBalance = liteEco.api.account().getBalance(player.uniqueId, currency).toDouble()

                EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null)
            }
        } catch (e: Exception) {
            liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "Error withdrawing from ${player.name}: ${e.message}")
            EconomyResponse(0.0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.message)
        }
    }

    override fun withdrawPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(player, amount)
    }

    override fun depositPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null || amount <= 0.0) {
            val currentBalance = if (player != null) getBalance(player) else 0.0
            return EconomyResponse(0.0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Invalid amount or player")
        }

        val currency = liteEco.currencyImpl.defaultCurrency()
        val amountBd = amount.toBigDecimal()

        return try {
            runBlocking {
                val currentBalanceBd = liteEco.api.account().getBalance(player.uniqueId, currency)

                if (liteEco.currencyImpl.getCheckBalanceLimit(currentBalanceBd, currency, amountBd)) {
                    return@runBlocking EconomyResponse(
                        0.0,
                        currentBalanceBd.toDouble(),
                        EconomyResponse.ResponseType.FAILURE,
                        FAIL_REACHED_BALANCE_LIMIT
                    )
                }

                liteEco.api.account().deposit(player.uniqueId, currency, amountBd)
                val newBalance = liteEco.api.account().getBalance(player.uniqueId, currency).toDouble()

                EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null)
            }
        } catch (e: Exception) {
            liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "Error depositing to ${player.name}: ${e.message}")
            EconomyResponse(0.0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.message)
        }
    }

    override fun depositPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return depositPlayer(player, amount)
    }

    override fun createPlayerAccount(player: OfflinePlayer?): Boolean {
        if (player == null) return false
        val playerName = player.name ?: "Unknown"

        return try {
            runBlocking {
                liteEco.api.createOrUpdateAccount(
                    player.uniqueId,
                    playerName,
                    liteEco.currencyImpl.defaultCurrency(),
                    liteEco.currencyImpl.defaultStartBalance()
                )
            }
        } catch (e: Exception) {
            liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "Error creating account for ${player.name}: ${e.message}")
            false
        }
    }

    override fun createPlayerAccount(player: OfflinePlayer?, worldName: String?): Boolean {
        return createPlayerAccount(player)
    }

    override fun createBank(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun deleteBank(name: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankBalance(name: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankHas(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankWithdraw(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankDeposit(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun isBankOwner(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun isBankMember(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun getBanks(): MutableList<String> {
        return mutableListOf()
    }
}