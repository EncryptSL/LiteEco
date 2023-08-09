package encryptsl.cekuj.net.hook.vault

import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit

abstract class DeprecatedEconomy : AbstractEconomy()  {

    companion object {
        private const val BANK_NOT_SUPPORTED_MESSAGE = "LiteEco does not support bank accounts!"
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?): Boolean {
        return hasAccount(Bukkit.getOfflinePlayer(playerName.toString()))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?, worldName: String?): Boolean {
        return hasAccount(Bukkit.getOfflinePlayer(playerName.toString()))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?): Double {
        return getBalance(Bukkit.getOfflinePlayer(playerName.toString()))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?, world: String?): Double {
        return getBalance(Bukkit.getOfflinePlayer(playerName.toString()))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, amount: Double): Boolean {
        return has(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
        return has(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        return depositPlayer(Bukkit.getOfflinePlayer(playerName.toString()), amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(player)"))
    override fun createPlayerAccount(playerName: String?): Boolean {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName.toString()))
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(player)"))
    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
        return createPlayerAccount(Bukkit.getOfflinePlayer(playerName.toString()))
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