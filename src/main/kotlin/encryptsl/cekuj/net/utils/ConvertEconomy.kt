package encryptsl.cekuj.net.utils

import encryptsl.cekuj.net.LiteEco
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConvertEconomy(private val liteEco: LiteEco) {

    private var converted = 0
    private var balances = 0.0

    fun convertEssentialsXEconomy() {
        Bukkit.getOfflinePlayers().forEach { p ->
            val playerFile =  File("plugins/Essentials/userdata/", "${p.uniqueId}.yml")
            if (playerFile.exists()) {
                val essentialsXConfig = YamlConfiguration.loadConfiguration(playerFile)
                val balance = essentialsXConfig.getString("money") ?: "0.0"
                liteEco.api.setMoney(p, balance.toDouble())
                balances = balance.toDouble()
                converted += 1
            }
        }
    }
    fun getResult(): EconomyConvertResult {
        return EconomyConvertResult(converted, balances)
    }
    fun convertRefresh() { converted = 0 }
}

data class EconomyConvertResult(val converted: Int, val balances: Double)