package com.github.encryptsl.lite.eco.common.hook.vaultunlocked

import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import java.math.BigDecimal
import java.util.*

abstract class UnusedVaultUnlockedAPI : Economy {
    companion object {
        private const val BANK_NOT_SUPPORTED_MESSAGE = "LiteEco does not support bank accounts!"
    }

    override fun createBank(p0: String?, p1: String?, p2: UUID?): Boolean {
        return false
    }

    override fun deleteBank(p0: String?, p1: UUID?): Boolean {
        return false
    }

    override fun getBankUUIDNameMap(): MutableMap<UUID, String> {
        return emptyMap<UUID, String>().toMutableMap()
    }

    override fun getBankAccountName(p0: UUID?): String {
        return ""
    }

    override fun hasBankAccount(p0: UUID?): Boolean {
        return false
    }

    override fun bankSupportsCurrency(p0: UUID?, p1: String?): Boolean {
        return false
    }

    override fun renameBankAccount(p0: String?, p1: UUID?, p2: String?): Boolean {
        return false
    }

    override fun bankBalance(p0: String?, p1: UUID?): BigDecimal {
        return BigDecimal.ZERO
    }

    override fun bankBalance(p0: String?, p1: UUID?, p2: String?): BigDecimal {
        return BigDecimal.ZERO
    }

    override fun bankHas(p0: String?, p1: UUID?, p2: BigDecimal?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun bankHas(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun bankWithdraw(p0: String?, p1: UUID?, p2: BigDecimal?): EconomyResponse {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankWithdraw(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): EconomyResponse {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankDeposit(p0: String?, p1: UUID?, p2: BigDecimal?): EconomyResponse {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun bankDeposit(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): EconomyResponse {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    override fun isBankOwner(p0: UUID?, p1: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun isBankMember(p0: UUID?, p1: UUID?): Boolean {
        return EconomyResponse(BigDecimal.ZERO, BigDecimal.ZERO, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE).transactionSuccess()
    }

    override fun getBanks(): MutableCollection<UUID> {
        TODO("Not yet implemented")
    }
}