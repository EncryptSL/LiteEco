@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.common.hook.vault.legacy

import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

@Suppress("DEPRECATION")
abstract class LegacyDeprecatedEconomy : Economy {

    companion object {
        private const val BANK_NOT_SUPPORTED_MESSAGE = "LiteEco does not support bank accounts!"
    }

    private fun getOfflinePlayerSafely(playerName: String?): OfflinePlayer? {
        if (playerName.isNullOrBlank()) return null
        return Bukkit.getOfflinePlayerIfCached(playerName) ?: Bukkit.getOfflinePlayer(playerName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return hasAccount(player)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?, worldName: String?): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return hasAccount(player, worldName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?): Double {
        val player = getOfflinePlayerSafely(playerName) ?: return 0.0
        return getBalance(player)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?, world: String?): Double {
        val player = getOfflinePlayerSafely(playerName) ?: return 0.0
        return getBalance(player, world)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, amount: Double): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return has(player, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return has(player, worldName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        val player = getOfflinePlayerSafely(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return withdrawPlayer(player, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        val player = getOfflinePlayerSafely(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return withdrawPlayer(player, worldName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        val player = getOfflinePlayerSafely(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return depositPlayer(player, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        val player = getOfflinePlayerSafely(playerName)
            ?: return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "Player not found")
        return depositPlayer(player, worldName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(player)"))
    override fun createPlayerAccount(playerName: String?): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return createPlayerAccount(player)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(player)"))
    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
        val player = getOfflinePlayerSafely(playerName) ?: return false
        return createPlayerAccount(player, worldName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createBank(name, player)"))
    override fun createBank(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("isBankOwner(name, player)"))
    override fun isBankOwner(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("isBankMember(name, player)"))
    override fun isBankMember(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, BANK_NOT_SUPPORTED_MESSAGE)
    }
}