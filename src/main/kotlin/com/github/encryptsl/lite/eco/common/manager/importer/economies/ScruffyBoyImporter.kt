package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.scruffyboy13.ScruffyboyEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

class ScruffyBoyImporter : EconomyImporter {
    override val name = "ScruffyBoyEconomy"

    override fun import(currency: String, liteEco: LiteEco, offlinePlayers: Array<OfflinePlayer>): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        try {
            val scruffyboyEconomy = ScruffyboyEconomyHook(liteEco)
            for (p in offlinePlayers) {
                val balance = BigDecimal.valueOf(scruffyboyEconomy.getBalance(p.uniqueId))
                if (liteEco.api.createAccount(p, currency, balance)) {
                    balances += balance
                    converted++
                }
            }
        } catch (e: Exception) {
            liteEco.logger.severe("ScruffyBoy import error: ${e.message ?: e.localizedMessage}")
        }

        return EconomyImportResults(converted, balances)
    }
}