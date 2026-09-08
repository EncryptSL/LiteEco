package com.github.encryptsl.lite.eco.common.hook.vault.unlocked.async

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import net.milkbowl.vault2.economy.EconomyResponse
import net.milkbowl.vault2.economy.MultiEconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class AsyncEconomyVaultUnlockedAPI(
    private val liteEco: LiteEco
) : AsyncUnusedVaultUnlockedAPI() {

    companion object {
        private const val AMOUNT_APPROACHING_ZERO = "LiteEco negative amounts are not allowed."
        private const val FAIL_REACHED_BALANCE_LIMIT = "LiteEco you reach limit of balance."
        private const val SUCCESS_WITHDRAW = "LiteEco withdraw was success."
        private const val FAIL_WITHDRAW = "LiteEco something happened in withdraw process."
        private const val SUCCESS_DEPOSIT = "LiteEco deposited amount was success."
        private const val FAIL_DEPOSIT = "LiteEco something happened in deposit process."
        private const val SUCCESS_SET = "LiteEco set amount was success."
        private const val FAIL_SET = "LiteEco something happened in set process."

        private const val FAIL_TRANSFER = "LiteEco transfer process failed."
        private const val SUCCESS_TRANSFER = "LiteEco transfer was success."
        private const val FAIL_TRANSFER_TARGET_NOT_FOUND = "LiteEco transfer aborted: Target account does not exist."
        private const val FAIL_TRANSFER_LIMIT_REACHED = "LiteEco transfer aborted: target reached balance limit. Money refunded."
        private const val FAIL_TRANSFER_INSUFFICIENT_FUNDS = "LiteEco transfer aborted: sender has insufficient funds."
    }

    private fun BigDecimal.isApproachingZero(): Boolean = this.signum() <= 0

    override fun createAccount(accountID: UUID, name: String, player: Boolean): CompletableFuture<Boolean> = liteEco.pluginScope.future {
        if (!player) return@future false
        val offlinePlayer = Bukkit.getOfflinePlayer(accountID)
        liteEco.api.createOrUpdateAccount(
            accountID,
            offlinePlayer.name ?: name,
            liteEco.currencyImpl.defaultCurrency(),
            liteEco.currencyImpl.defaultStartBalance()
        )
    }

    override fun createAccount(accountID: UUID, name: String, worldName: String, player: Boolean): CompletableFuture<Boolean> {
        return createAccount(accountID, name, player)
    }

    override fun getUUIDNameMap(): CompletableFuture<Map<UUID, String>> = liteEco.pluginScope.future {
        liteEco.api.getUUIDNameMap()
    }

    override fun getAccountName(accountID: UUID): CompletableFuture<Optional<String>> = liteEco.pluginScope.future {
        val user = liteEco.api.account().getUserByUUID(accountID, liteEco.currencyImpl.defaultCurrency())
        Optional.ofNullable(user?.userName)
    }

    override fun hasAccount(accountID: UUID): CompletableFuture<Boolean> = liteEco.pluginScope.future {
        val user = liteEco.api.account().getUserByUUID(accountID, liteEco.currencyImpl.defaultCurrency())
        user != null
    }

    override fun hasAccount(accountID: UUID, worldName: String): CompletableFuture<Boolean> {
        return hasAccount(accountID)
    }

    override fun renameAccount(pluginName: String, accountID: UUID, name: String): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(false)
    }

    override fun deleteAccount(pluginName: String, accountID: UUID): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(false)
    }

    override fun accountSupportsCurrency(pluginName: String, accountID: UUID, currency: String): CompletableFuture<Boolean> = liteEco.pluginScope.future {
        try {
            liteEco.api.account().getUserByUUID(accountID, currency) != null
        } catch (_: Exception) {
            false
        }
    }

    override fun accountSupportsCurrency(pluginName: String, accountID: UUID, currency: String, world: String): CompletableFuture<Boolean> {
        return accountSupportsCurrency(pluginName, accountID, currency)
    }

    override fun balance(pluginName: String, accountID: UUID): CompletableFuture<BigDecimal> = liteEco.pluginScope.future {
        liteEco.api.account().getBalance(accountID, liteEco.currencyImpl.defaultCurrency())
    }

    override fun balance(pluginName: String, accountID: UUID, world: String): CompletableFuture<BigDecimal> {
        return balance(pluginName, accountID)
    }

    override fun balance(pluginName: String, accountID: UUID, world: String, currency: String): CompletableFuture<BigDecimal> = liteEco.pluginScope.future {
        liteEco.api.account().getBalance(accountID, currency)
    }

    override fun has(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<Boolean> = liteEco.pluginScope.future {
        liteEco.api.account().has(accountID, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun has(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<Boolean> {
        return has(pluginName, accountID, amount)
    }

    override fun has(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<Boolean> = liteEco.pluginScope.future {
        liteEco.api.account().has(accountID, currency, amount)
    }

    override fun set(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return set(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun set(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return set(pluginName, accountID, world, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun set(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<EconomyResponse> = liteEco.pluginScope.future {
        liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName try async set $accountID amount $amount ($currency)")

        val balanceBefore = liteEco.api.account().getBalance(accountID, currency)
        if (amount.isApproachingZero()) {
            return@future EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        try {
            val user = liteEco.api.account().getUserByUUID(accountID, currency)
            if (user != null) {
                if (liteEco.currencyImpl.getCheckBalanceLimit(amount, currency)) {
                    return@future EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
                }

                liteEco.api.account().set(accountID, currency, amount)
                liteEco.loggerModel.logging(
                    TransactionContextEntity(
                        TypeLogger.SET,
                        pluginName,
                        user.userName,
                        currency,
                        balanceBefore,
                        amount
                    )
                )
                liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName successfully async set $accountID balance to $amount")

                EconomyResponse(amount, amount, EconomyResponse.ResponseType.SUCCESS, SUCCESS_SET)
            } else {
                EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, FAIL_SET)
            }
        } catch (e: Exception) {
            liteEco.logger.error("VaultUnlocked async set error: ${e.message}")
            EconomyResponse(amount, balanceBefore, EconomyResponse.ResponseType.FAILURE, FAIL_SET)
        }
    }

    override fun canWithdraw(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return canWithdraw(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun canWithdraw(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return canWithdraw(pluginName, accountID, world, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun canWithdraw(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<EconomyResponse> = liteEco.pluginScope.future {
        val currentBalance = liteEco.api.account().getBalance(accountID, currency)
        if (amount.isApproachingZero()) {
            return@future EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }
        if (liteEco.api.account().has(accountID, currency, amount)) {
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.SUCCESS, "Can withdraw.")
        } else {
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.")
        }
    }

    override fun withdraw(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return withdraw(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return withdraw(pluginName, accountID, world, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun withdraw(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<EconomyResponse> = liteEco.pluginScope.future {
        liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName try async withdraw from $accountID amount $amount ($currency)")

        val currentBalance = liteEco.api.account().getBalance(accountID, currency)
        if (amount.isApproachingZero()) {
            return@future EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        try {
            if (liteEco.api.account().has(accountID, currency, amount)) {
                val user = liteEco.api.account().getUserByUUID(accountID, currency)
                val username = user?.userName ?: Bukkit.getOfflinePlayer(accountID).name ?: "Unknown"

                liteEco.api.account().withdraw(accountID, currency, amount)

                val balanceAfter = currentBalance.subtract(amount)
                liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.WITHDRAW, pluginName, username, currency, currentBalance, balanceAfter))
                liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName successfully async withdraw $accountID. New balance: $balanceAfter")

                EconomyResponse(amount, balanceAfter, EconomyResponse.ResponseType.SUCCESS, SUCCESS_WITHDRAW)
            } else {
                EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
            }
        } catch (e: Exception) {
            liteEco.logger.error("VaultUnlocked async withdraw error: ${e.message}")
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_WITHDRAW)
        }
    }

    override fun canDeposit(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return canDeposit(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun canDeposit(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return canDeposit(pluginName, accountID, world, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun canDeposit(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<EconomyResponse> = liteEco.pluginScope.future {
        val currentBalance = liteEco.api.account().getBalance(accountID, currency)
        if (amount.isApproachingZero()) {
            return@future EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }
        if (liteEco.currencyImpl.getCheckBalanceLimit(currentBalance, currency, amount)) {
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
        } else {
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.SUCCESS, "Can deposit.")
        }
    }

    override fun deposit(pluginName: String, accountID: UUID, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return deposit(pluginName, accountID, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, world: String, amount: BigDecimal): CompletableFuture<EconomyResponse> {
        return deposit(pluginName, accountID, world, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun deposit(pluginName: String, accountID: UUID, world: String, currency: String, amount: BigDecimal): CompletableFuture<EconomyResponse> = liteEco.pluginScope.future {
        liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName try async deposit to $accountID amount $amount ($currency)")

        val currentBalance = liteEco.api.account().getBalance(accountID, currency)
        if (amount.isApproachingZero()) {
            return@future EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        try {
            val user = liteEco.api.account().getUserByUUID(accountID, currency)
            if (user != null) {
                if (liteEco.currencyImpl.getCheckBalanceLimit(currentBalance, currency, amount)) {
                    return@future EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_REACHED_BALANCE_LIMIT)
                }

                liteEco.api.account().deposit(accountID, currency, amount)

                val balanceAfter = currentBalance.add(amount)
                liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.DEPOSIT, pluginName, user.userName, currency, currentBalance, balanceAfter))
                liteEco.debugger.debug(AsyncEconomyVaultUnlockedAPI::class.java, "$pluginName successfully async deposit to $accountID. New balance: $balanceAfter")

                EconomyResponse(amount, balanceAfter, EconomyResponse.ResponseType.SUCCESS, SUCCESS_DEPOSIT)
            } else {
                EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
            }
        } catch (e: Exception) {
            liteEco.logger.error("VaultUnlocked async deposit error: ${e.message}")
            EconomyResponse(amount, currentBalance, EconomyResponse.ResponseType.FAILURE, FAIL_DEPOSIT)
        }
    }

    override fun transfer(pluginName: String, from: UUID, to: UUID, amount: BigDecimal): CompletableFuture<MultiEconomyResponse> {
        return transfer(pluginName, from, to, "default", liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun transfer(pluginName: String, from: UUID, to: UUID, worldName: String, amount: BigDecimal): CompletableFuture<MultiEconomyResponse> {
        return transfer(pluginName, from, to, worldName, liteEco.currencyImpl.defaultCurrency(), amount)
    }

    override fun transfer(pluginName: String, from: UUID, to: UUID, worldName: String, currency: String, amount: BigDecimal): CompletableFuture<MultiEconomyResponse> = liteEco.pluginScope.future {
        if (amount.isApproachingZero()) {
            return@future MultiEconomyResponse(amount, EconomyResponse.ResponseType.FAILURE, AMOUNT_APPROACHING_ZERO)
        }

        try {
            if (liteEco.api.account().getUserByUUID(to, currency) == null) {
                return@future MultiEconomyResponse(amount, EconomyResponse.ResponseType.FAILURE, FAIL_TRANSFER_TARGET_NOT_FOUND)
            }

            val withdrawRes = withdraw(pluginName, from, worldName, currency, amount).await()

            if (withdrawRes.transactionSuccess()) {
                val depositRes = deposit(pluginName, to, worldName, currency, amount).await()

                if (depositRes.transactionSuccess()) {
                    MultiEconomyResponse(amount, withdrawRes.type, SUCCESS_TRANSFER)
                } else {
                    try {
                        liteEco.api.account().deposit(from, currency, amount)
                    } catch (e: Exception) {
                        liteEco.logger.error("Failed to refund money back to $from during transfer: ${e.message}")
                    }

                    MultiEconomyResponse(amount, EconomyResponse.ResponseType.FAILURE, FAIL_TRANSFER_LIMIT_REACHED)
                }
            } else {
                MultiEconomyResponse(amount, EconomyResponse.ResponseType.FAILURE, FAIL_TRANSFER_INSUFFICIENT_FUNDS)
            }
        } catch (e: Exception) {
            liteEco.logger.error("VaultUnlocked async transfer error: ${e.message}")
            MultiEconomyResponse(amount, EconomyResponse.ResponseType.FAILURE, FAIL_TRANSFER)
        }
    }
}