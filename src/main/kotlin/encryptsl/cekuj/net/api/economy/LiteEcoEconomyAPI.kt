package encryptsl.cekuj.net.api.economy

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.interfaces.LiteEconomyAPIProvider
import encryptsl.cekuj.net.extensions.moneyFormat
import org.bukkit.OfflinePlayer

class LiteEcoEconomyAPI(private val liteEco: LiteEco) : LiteEconomyAPIProvider {
    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        if (hasAccount(player)) {
            return false
        }

        liteEco.preparedStatements.createPlayerAccount(player.uniqueId, startAmount)
        return true
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        if (!hasAccount(player)) {
            return false
        }

        liteEco.preparedStatements.deletePlayerAccount(player.uniqueId)
        return true
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return liteEco.preparedStatements.getExistPlayerAccount(player.uniqueId)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return amount <= getBalance(player)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return if(hasAccount(player)) liteEco.preparedStatements.getBalance(player.uniqueId) else 0.0
    }

    override fun depositMoney(player: OfflinePlayer, amount: Double) {
        liteEco.preparedStatements.depositMoney(player.uniqueId, getBalance(player).plus(amount))
    }

    override fun withDrawMoney(player: OfflinePlayer, amount: Double) {
        liteEco.preparedStatements.withdrawMoney(player.uniqueId, getBalance(player).minus(amount))
    }

    override fun setMoney(player: OfflinePlayer, amount: Double) {
        liteEco.preparedStatements.setMoney(player.uniqueId, amount)
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        return liteEco.preparedStatements.getTopBalance()
    }

    override fun formatting(amount: Double): String {
        return amount.moneyFormat(liteEco.config.getString("plugin.economy.prefix").toString(), liteEco.config.getString("plugin.economy.name").toString())
    }
}