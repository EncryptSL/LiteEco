package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConvertEconomy(private val liteEco: LiteEco) {

    private var converted = 0
    private var balances = 0.0

    enum class Economies { EssentialsX, BetterEconomy, }

    fun convertEssentialsXEconomy() {
        for (p in Bukkit.getOfflinePlayers()) {
            val playerFile =  File("plugins/Essentials/userdata/", "${p.uniqueId}.yml")
            if (playerFile.exists()) {
                val essentialsXConfig = YamlConfiguration.loadConfiguration(playerFile)
                val balance = essentialsXConfig.getString("money") ?: "0.0"
                if (liteEco.api.createAccount(p, balance.toDouble())) {
                    balances = balance.toDouble()
                    converted += 1
                }
            }
        }
    }

    fun convertBetterEconomy() {
        try {
            val betterEconomy = BetterEconomyHook()
            for (p in Bukkit.getOfflinePlayers()) {
                val balance = betterEconomy.getBalance(p.uniqueId)
                if (liteEco.api.createAccount(p, balance)) {
                    balances = balance
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

    data class EconomyConvertResult(val converted: Int, val balances: Double)
}
