package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.theosiseconomy.TheosisEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.launch
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import kotlin.plus

class TheosisEconomyImporter : EconomyImporter {

    override val name = "TheosisEconomy"

    override fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        try {
            liteEco.pluginScope.launch {
                val theosisEconomyHook = TheosisEconomyHook(liteEco)
                for (p in offlinePlayers) {
                    val balance = theosisEconomyHook.getBalance(p.uniqueId)
                    if (liteEco.api.createOrUpdateAccount(p.uniqueId, p.name.toString(), currency, balance)) {
                        balances += balance
                        converted++
                    }
                }
            }
        } catch (e: Exception) {
            liteEco.logger.severe("TheosisEconomy import error: ${e.message ?: e.localizedMessage}")
        }

        return EconomyImportResults(converted, balances)
    }

}