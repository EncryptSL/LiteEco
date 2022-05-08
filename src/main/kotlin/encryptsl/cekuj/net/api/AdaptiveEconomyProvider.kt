package encryptsl.cekuj.net.api

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.extensions.isNegative
import encryptsl.cekuj.net.extensions.moneyFormat
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.OfflinePlayer
import java.util.*


@Suppress("DEPRECATION")
class AdaptiveEconomyProvider(private val liteEco: LiteEco) : Economy {

    override fun isEnabled(): Boolean {
        return liteEco.isEnabled
    }

    override fun getName(): String {
        return "LiteEco"
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    override fun format(amount: Double): String {
        return amount.moneyFormat(currencyNameSingular().toString(), currencyNamePlural().toString())
    }

    override fun currencyNamePlural(): String? {
        return liteEco.config.getString("plugin.economy.name")
    }

    override fun currencyNameSingular(): String? {
        return liteEco.config.getString("plugin.economy.prefix")
    }

    override fun hasAccount(player: OfflinePlayer?): Boolean {
        return liteEco.preparedStatements.getExistPlayerAccount(player!!.uniqueId)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?): Boolean {
        return hasAccount(playerName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("hasAccount(player)"))
    override fun hasAccount(playerName: String?, worldName: String?): Boolean {
        return hasAccount(playerName)
    }

    override fun hasAccount(player: OfflinePlayer?, worldName: String?): Boolean {
        return hasAccount(player)
    }

    override fun getBalance(player: OfflinePlayer?): Double {
        return liteEco.preparedStatements.getBalance(player!!.uniqueId)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?): Double {
        return getBalance(playerName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBalance(player)"))
    override fun getBalance(playerName: String?, world: String?): Double {
        return getBalance(playerName)
    }

    override fun getBalance(player: OfflinePlayer?, world: String?): Double {
        return getBalance(player)
    }

    override fun has(player: OfflinePlayer?, amount: Double): Boolean {
        return (getBalance(player) >= amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, amount: Double): Boolean {
        return has(playerName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("has(player, amount)"))
    override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
        return has(playerName, amount)
    }

    override fun has(player: OfflinePlayer?, worldName: String?, amount: Double): Boolean {
        return has(player, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, liteEco.translationConfig.getMessage("messages.player_is_null_error"))
        }
        if (amount.isNegative()) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, liteEco.translationConfig.getMessage("messages.negative_amount_error"))
        }

        return if (has(player, amount)) {
            liteEco.preparedStatements.withdrawMoney(player.uniqueId, amount)
            EconomyResponse(amount, getBalance(player), ResponseType.SUCCESS, null)
        } else {
            EconomyResponse(0.0, getBalance(player), ResponseType.FAILURE, liteEco.translationConfig.getMessage("messages.sender_error_enough_pay"))
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("withdrawPlayer(player, amount)"))
    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return withdrawPlayer(player, amount)
    }

    override fun depositPlayer(player: OfflinePlayer?, amount: Double): EconomyResponse {
        if (player == null) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, liteEco.translationConfig.getMessage("messages.player_is_null_error"))
        }

        if (amount.isNegative()) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, liteEco.translationConfig.getMessage("messages.negative_amount_error"))
        }

        liteEco.preparedStatements.depositMoney(player.uniqueId, amount)

        return EconomyResponse(amount, getBalance(player), ResponseType.SUCCESS, null)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("depositPlayer(player, amount)"))
    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    override fun depositPlayer(player: OfflinePlayer?, worldName: String?, amount: Double): EconomyResponse {
        return depositPlayer(player, amount)
    }

    override fun createPlayerAccount(player: OfflinePlayer?): Boolean {
        if (hasAccount(player)) {
            return false
        }
        liteEco.preparedStatements.createPlayerAccount(player!!.uniqueId, liteEco.config.getDouble("plugin.economy.default_money"))
        return true
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(player)"))
    override fun createPlayerAccount(playerName: String?): Boolean {
        return createPlayerAccount(playerName)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("createPlayerAccount(playerName)"))
    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
        return createPlayerAccount(playerName)
    }

    override fun createPlayerAccount(player: OfflinePlayer?, worldName: String?): Boolean {
        return createPlayerAccount(player)
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, \"LiteEco does not support bank accounts!\")",
        "net.milkbowl.vault.economy.EconomyResponse",
        "net.milkbowl.vault.economy.EconomyResponse"
    )
    )
    override fun createBank(name: String?, player: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun createBank(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun deleteBank(name: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun bankBalance(name: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun bankHas(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun bankWithdraw(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun bankDeposit(name: String?, amount: Double): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, \"LiteEco does not support bank accounts!\")",
        "net.milkbowl.vault.economy.EconomyResponse",
        "net.milkbowl.vault.economy.EconomyResponse"
    )
    )
    override fun isBankOwner(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun isBankOwner(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, \"LiteEco does not support bank accounts!\")",
        "net.milkbowl.vault.economy.EconomyResponse",
        "net.milkbowl.vault.economy.EconomyResponse"
    )
    )
    override fun isBankMember(name: String?, playerName: String?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun isBankMember(name: String?, player: OfflinePlayer?): EconomyResponse {
        return EconomyResponse(0.0, 0.0, ResponseType.NOT_IMPLEMENTED, "LiteEco does not support bank accounts!")
    }

    override fun getBanks(): MutableList<String> {
        return Collections.emptyList()
    }

}