package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class ConvertEconomy(private val liteEco: LiteEco) {

    private var converted = 0
    private var balances = BigDecimal.ZERO

    enum class Economies { EssentialsX, BetterEconomy, }

    fun convertEssentialsXEconomy(currency: String) {
        for (p in Bukkit.getOfflinePlayers()) {
            val playerFile =  File("plugins/Essentials/userdata/", "${p.uniqueId}.yml")
            if (playerFile.exists()) {
                val essentialsXConfig = YamlConfiguration.loadConfiguration(playerFile)
                val balance = essentialsXConfig.getString("money") ?: BigDecimal.valueOf(0.00).toPlainString()
                if (liteEco.api.createAccount(p, currency, balance.toBigDecimal())) {
                    balances += balance.toBigDecimal()
                    converted += 1
                }
            }
        }
    }

    fun convertBetterEconomy(currency: String) {
        try {
            val betterEconomy = BetterEconomyHook()
            for (p in Bukkit.getOfflinePlayers()) {
                val balance = BigDecimal.valueOf(betterEconomy.getBalance(p.uniqueId))
                if (liteEco.api.createAccount(p, currency, balance)) {
                    balances += balance
                    converted += 1
                }
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    fun getResult(): EconomyConvertResult {
        return EconomyConvertResult(converted, balances)
    }
    fun convertRefresh() { converted = 0 }

    data class EconomyConvertResult(val converted: Int, val balances: BigDecimal)
}
