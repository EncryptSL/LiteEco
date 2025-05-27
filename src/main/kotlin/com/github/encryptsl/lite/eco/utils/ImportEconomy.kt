package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.hook.craftconomy3.CraftConomyHook
import com.github.encryptsl.lite.eco.common.hook.scruffyboy13.ScruffyboyEconomyHook
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class ImportEconomy(private val liteEco: LiteEco) {

    private var converted = 0
    private var balances = BigDecimal.ZERO

    private var offlinePlayers = Bukkit.getOfflinePlayers()

    enum class Economies { EssentialsX, BetterEconomy, ScruffyBoyEconomy, CraftConomy3 }

    fun importEssentialsXEconomy(currency: String) {
        for (p in offlinePlayers) {
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

    fun importBetterEconomy(currency: String) {
        try {
            val betterEconomy = BetterEconomyHook(liteEco)
            for (p in offlinePlayers) {
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

    fun importScruffyBoyEconomy(currency: String) {
        try {
            val scruffyboyEconomy = ScruffyboyEconomyHook(liteEco)
            for (p in offlinePlayers) {
                val balance = BigDecimal.valueOf(scruffyboyEconomy.getBalance(p.uniqueId))
                if (liteEco.api.createAccount(p, currency, balance)) {
                    balances += balance
                    converted += 1
                }
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    fun importCraftConomy3(currency: String) {
        try {
            val craftConomy = CraftConomyHook(liteEco)
            for (p in offlinePlayers) {
                val balance = BigDecimal.valueOf(craftConomy.getBalance(p.name.toString()))
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
