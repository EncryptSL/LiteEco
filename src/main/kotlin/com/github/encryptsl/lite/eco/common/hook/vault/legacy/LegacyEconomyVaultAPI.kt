package com.github.encryptsl.lite.eco.common.hook.vault.legacy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import java.util.*

@Suppress("DEPRECATION")
class LegacyEconomyVaultAPI(private val liteEco: LiteEco) : LegacyDeprecatedEconomy() {

    companion object {
        private const val BANK_NOT_SUPPORTED_MESSAGE = "LiteEco does not support bank accounts!"
        private const val FAIL_REACHED_BALANCE_LIMIT = "LiteEco you reach limit of balance."
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name

    override fun hasBankSupport(): Boolean = false

    override fun fractionalDigits(): Int = -1

    override fun format(amount: Double): String = liteEco.currencyImpl.fullFormatting(amount.toBigDecimal())

    override fun currencyNamePlural(): String? {
        return null
    }

    override fun currencyNameSingular(): String? {
        return null
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return liteEco.api.hasAccount(player.uniqueId)
    }

    override fun hasAccount(player: OfflinePlayer, worldName: String?): Boolean {
        return hasAccount(player)
    }

    override fun getBalance(player: OfflinePlayer?): Double {
        if (player == null) return 0.0

        return try {
            runBlocking {
                liteEco.api.getBalance(player.uniqueId, liteEco.currencyImpl.defaultCurrency()).toDouble()
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
        return if(player != null) liteEco.api.has(player.uniqueId, liteEco.currencyImpl.defaultCurrency(), amount.toBigDecimal()) else false
    }

    override fun has(player: OfflinePlayer?, worldName: String?, amount: Double): Boolean {
        return has(player, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "try withdraw from ${player?.name} amount $amount")
        if (player == null || amount.toBigDecimal().isApproachingZero()) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, null)
        }

        return if (has(player, amount)) {
            liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "successfully withdraw ${player.name} from his balance ${getBalance(player)} amount $amount")
            runBlocking {
                liteEco.api.withdraw(player.uniqueId, liteEco.currencyImpl.defaultCurrency(), amount.toBigDecimal())
            }
            EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(0.0, getBalance(player), EconomyResponse.ResponseType.FAILURE, null)
        }
    }

    override fun withdrawPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(player, amount)
    }

    override fun depositPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "try deposit to ${player?.name} amount $amount")
        if (player == null || !hasAccount(player) || amount.toBigDecimal().isApproachingZero() || liteEco.currencyImpl.getCheckBalanceLimit(getBalance(player).toBigDecimal(), amount = amount.toBigDecimal())) {
            return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, null)
        }

        liteEco.debugger.debug(LegacyEconomyVaultAPI::class.java, "successfully deposit ${player.name} to his balance ${getBalance(player)} amount $amount")

        val currencyName = liteEco.currencyImpl.defaultCurrency()
        val currentBalance = this.getBalance(player, currencyName).toBigDecimal()

        if (liteEco.currencyImpl.getCheckBalanceLimit(currentBalance, currencyName, amount.toBigDecimal())) {
            return EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
        }

        runBlocking {
            liteEco.api.deposit(player.uniqueId, liteEco.currencyImpl.defaultCurrency(), amount.toBigDecimal())
        }
        return EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null)
    }

    override fun depositPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return depositPlayer(player, amount)
    }

    override fun createPlayerAccount(player: OfflinePlayer?): Boolean {
        return runBlocking {
            liteEco.api.createOrUpdateAccount(
                player!!.uniqueId,
                player.name.toString(),
                liteEco.currencyImpl.defaultCurrency(),
                liteEco.currencyImpl.defaultStartBalance()
            )
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
        return Collections.emptyList()
    }
}