package encryptsl.cekuj.net.api.economy

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.PlayerAccount
import encryptsl.cekuj.net.api.interfaces.LiteEconomyAPIProvider
import encryptsl.cekuj.net.extensions.compactFormat
import encryptsl.cekuj.net.extensions.moneyFormat
import org.bukkit.OfflinePlayer
import java.util.*

class LiteEcoEconomyAPI(private val liteEco: LiteEco) : LiteEconomyAPIProvider {

    private val playerAccount: PlayerAccount by lazy { PlayerAccount(liteEco) }

    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        if (hasAccount(player)) return false

        liteEco.preparedStatements.createPlayerAccount(player.uniqueId, startAmount)
        return true
    }

    override fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean {
        if (!hasAccount(player)) return false

        playerAccount.cacheAccount(player.uniqueId, amount)
        return true
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        if (!hasAccount(player)) return false

        return if (playerAccount.isPlayerOnline(player.uniqueId)) {
            playerAccount.removeAccount(player.uniqueId)
            true
        } else {
            liteEco.preparedStatements.deletePlayerAccount(player.uniqueId)
            true
        }
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return liteEco.preparedStatements.getExistPlayerAccount(player.uniqueId)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return amount <= getBalance(player)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return if (playerAccount.isPlayerOnline(player.uniqueId) && playerAccount.isAccountCached(player.uniqueId))
            playerAccount.getAccount().getOrDefault(player.uniqueId, 0.0)
        else
            liteEco.preparedStatements.getBalance(player.uniqueId)
    }

    override fun depositMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            liteEco.preparedStatements.depositMoney(player.uniqueId, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            liteEco.preparedStatements.withdrawMoney(player.uniqueId, amount)
        }
    }

    override fun setMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            liteEco.preparedStatements.setMoney(player.uniqueId, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer) {
        playerAccount.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        playerAccount.syncAccounts()
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        return liteEco.preparedStatements.getTopBalance() // This must be same, because cache can be removed or cleared if player leave.
    }

    override fun compacted(amount: Double): String {
        return amount.compactFormat(liteEco.config.getString("formatting.currency_pattern").toString(), liteEco.config.getString("formatting.compact_pattern").toString(), liteEco.config.getString("formatting.currency_locale").toString())
    }

    override fun formatted(amount: Double): String {
        return amount.moneyFormat(liteEco.config.getString("formatting.currency_pattern").toString(), liteEco.config.getString("formatting.currency_locale").toString())
    }

    override fun fullFormatting(amount: Double): String {
        val value = if (liteEco.config.getBoolean("plugin.economy.compact_display")) {
            compacted(amount)
        }
        else {
            formatted(amount)
        }
        return "${liteEco.config.getString("economy.currency_prefix").toString()}${value} ${liteEco.config.getString("economy.currency_name").toString()}"
    }
}