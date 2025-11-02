package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.economy.craftconomy3.CraftConomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import java.math.BigDecimal

class CraftConomy3Importer : EconomyImporter {

    override val name = "CraftConomy3"

    override fun import(currency: String, liteEco: LiteEco, offlinePlayers: Array<OfflinePlayer>): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        try {
            runBlocking {
                val craftConomy = CraftConomyHook(liteEco)
                for (p in offlinePlayers) {
                    val balance = BigDecimal.valueOf(craftConomy.getBalance(p.name.toString()))
                    if (liteEco.api.createOrUpdateAccount(p.uniqueId, p.name.toString(), currency, balance)) {
                        balances += balance
                        converted++
                    }
                }
            }
        } catch (e: Exception) {
            liteEco.logger.severe("CraftConomy3 import error: ${e.message ?: e.localizedMessage}")
        }

        return EconomyImportResults(converted, balances)
    }
}