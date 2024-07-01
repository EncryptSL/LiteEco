package com.github.encryptsl.lite.eco.common.hook.vaultunlocked

import com.github.encryptsl.lite.eco.LiteEco
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class AdaptiveEconomyVaultUnlockedAPI(private val liteEco: LiteEco) : UnusedVaultUnlockedAPI() {

    override fun isEnabled(): Boolean = liteEco.isEnabled

    override fun getName(): String = liteEco.name


    override fun hasBankSupport(): Boolean = false

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(): Int {
        return 0
    }

    override fun format(value: BigDecimal?): String {
        return liteEco.api.formatted(value?.toDouble() ?: 0.0)
    }

    override fun format(value: BigDecimal?, p1: String?): String {
        return liteEco.api.formatted(value?.toDouble() ?: 0.0)
    }

    override fun hasCurrency(p0: String?): Boolean = false

    override fun getDefaultCurrency(): String {
        TODO("Not yet implemented")
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

    override fun hasAccount(p0: UUID?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasAccount(p0: UUID?, p1: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun renameAccount(p0: UUID?, p1: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getBalance(p0: String?, p1: UUID?): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBalance(p0: String?, p1: UUID?, p2: String?): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun getBalance(p0: String?, p1: UUID?, p2: String?, p3: String?): BigDecimal {
        TODO("Not yet implemented")
    }

    override fun has(p0: String?, p1: UUID?, p2: BigDecimal?): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(p0: String?, p1: UUID?, p2: String?, p3: String?, p4: BigDecimal?): Boolean {
        TODO("Not yet implemented")
    }

    override fun withdraw(p0: String?, p1: UUID?, p2: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdraw(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun withdraw(p0: String?, p1: UUID?, p2: String?, p3: String?, p4: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deposit(p0: String?, p1: UUID?, p2: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deposit(p0: String?, p1: UUID?, p2: String?, p3: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }

    override fun deposit(p0: String?, p1: UUID?, p2: String?, p3: String?, p4: BigDecimal?): EconomyResponse {
        TODO("Not yet implemented")
    }
}