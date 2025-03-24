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
        private const val AMOUNT_APPROACHING_ZERO = "LiteEco negative amounts are not allowed."
        private const val SUCCESS_WITHDRAW = "LiteEco withdraw was success."
        private const val FAIL_WITHDRAW = "LiteEco something happened in withdraw process."
        private const val SUCCESS_DEPOSIT = "LiteEco deposited amount was success."
        private const val FAIL_DEPOSIT = "LiteEco something happened in deposit process."
    }

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(pluginName: String): Int = -1

    override fun format(pluginName: String, amount: BigDecimal): String {
        return liteEco.api.formatted(amount)
    }

    override fun format(pluginName: String, amount: BigDecimal, currency: String): String = liteEco.api.formatted(amount)

    override fun hasCurrency(currencyName: String): Boolean = false

    override fun getDefaultCurrency(pluginName: String): String = liteEco.currencyImpl.defaultCurrency()

    override fun defaultCurrencyNamePlural(pluginName: String): String = ""

    override fun defaultCurrencyNameSingular(pluginName: String): String = ""

    override fun currencies(): MutableCollection<String> {
        return liteEco.currencyImpl.getCurrenciesKeys().toMutableList()
    }

    override fun createAccount(accountID: UUID, name: String, player: Boolean): Boolean {
        if (!player) {
            return false
        }
        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(accountID), startAmount = liteEco.currencyImpl.defaultStartBalance())
    }

    override fun createAccount(accountID: UUID, name: String, worldName: String, player: Boolean): Boolean {
        if (!player) {
            return false
        }

        return liteEco.api.createAccount(Bukkit.getOfflinePlayer(accountID), startAmount = liteEco.currencyImpl.defaultStartBalance())
    }

    override fun deleteAccount(plugin: String, accountID: UUID): Boolean = false

    override fun getUUIDNameMap(): MutableMap<UUID, String> {
        return liteEco.api.getUUIDNameMap()
    }

    override fun getAccountName(uuid: UUID): Optional<String> {
        val user = liteEco.api.getUserByUUID(uuid, liteEco.currencyImpl.defaultCurrency())
        return Optional.of(user.join().get().userName)
    }

    override fun hasAccount(uuid: UUID): Boolean {
        return liteEco.api.getUserByUUID(uuid, liteEco.currencyImpl.defaultCurrency()).thenApply {
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.get()
    }

    override fun hasAccount(accountID: UUID, worldName: String): Boolean = hasAccount(accountID)

    override fun renameAccount(accountID: UUID, name: String): Boolean = false

    override fun renameAccount(plugin: String, accountID: UUID, name: String): Boolean = false

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String): Boolean {
        return liteEco.api.hasAccount(accountID, currency).thenApply {
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun accountSupportsCurrency(plugin: String, accountID: UUID, currency: String, world: String): Boolean {
        return accountSupportsCurrency(plugin, accountID, currency)
    }

    override fun balance(pluginName: String, accountID: UUID): BigDecimal {
        return liteEco.api.getBalance(accountID, liteEco.currencyImpl.defaultCurrency())
    }

    override fun balance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        return balance(pluginName, accountID)
    }

    override fun balance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        return balance(pluginName, accountID)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID)"))
    override fun getBalance(pluginName: String, accountID: UUID): BigDecimal {
        return balance(pluginName, accountID)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String): BigDecimal {
        return balance(pluginName, accountID)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("balance(pluginName, accountID, world, currency)"))
    override fun getBalance(pluginName: String, accountID: UUID, world: String, currency: String): BigDecimal {
        return balance(pluginName, accountID, world, currency)
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
        return has(pluginName, accountID, amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        if (amount.isApproachingZero()) {
            return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        return if (has(pluginName, accountID, amount)) {
            liteEco.api.withDrawMoney(accountID, liteEco.currencyImpl.defaultCurrency(), amount)
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.SUCCESS, SUCCESS_WITHDRAW)
        } else {
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
        }
    }

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): EconomyResponse {

        if (amount.isApproachingZero()) {
            return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        return if (has(pluginName, accountID, amount)) {
            liteEco.api.withDrawMoney(accountID, liteEco.currencyImpl.defaultCurrency(), amount)
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.SUCCESS, SUCCESS_WITHDRAW)
        } else {
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
        }
    }

    override fun withdraw(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        return withdraw(pluginName, accountID, amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, amount: BigDecimal): EconomyResponse {
        if (amount.isApproachingZero()) {
            return EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }
        return if (has(pluginName, accountID, amount)) {
            liteEco.api.depositMoney(accountID, liteEco.currencyImpl.defaultCurrency(), amount)
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.SUCCESS, SUCCESS_DEPOSIT)
        } else {
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
        }
    }

    override fun deposit(pluginName: String, accountID: UUID, worldName: String, amount: BigDecimal): EconomyResponse {
        return deposit(pluginName, accountID, amount)
    }

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): EconomyResponse {
        if (amount.isApproachingZero()) {
            return EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        return if (has(pluginName, accountID, amount)) {
            liteEco.api.depositMoney(accountID, currency, amount)
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.SUCCESS, SUCCESS_DEPOSIT)
        } else {
            EconomyResponse(amount, balance(pluginName, accountID), EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
        }
    }
}