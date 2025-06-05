package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

class BetterEconomyImporter : EconomyImporter {
    override val name = "BetterEconomy"

    override fun import(currency: String, liteEco: LiteEco, offlinePlayers: Array<OfflinePlayer>): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        try {
            val betterEconomy = BetterEconomyHook(liteEco)
            for (p in offlinePlayers) {
                val balance = BigDecimal.valueOf(betterEconomy.getBalance(p.uniqueId))
                if (liteEco.api.createAccount(p, currency, balance)) {
                    balances += balance
                    converted++
                }
            }
        } catch (e: Exception) {
            liteEco.logger.severe("BetterEconomy import error: ${e.message ?: e.localizedMessage}")
        }

        return EconomyImportResults(converted, balances)
    }
}