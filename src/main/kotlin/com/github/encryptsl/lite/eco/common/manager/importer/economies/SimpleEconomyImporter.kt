package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy.SimpleEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.UUID
import kotlin.plus

class SimpleEconomyImporter : EconomyImporter {

    override val name: String = "SimpleEconomy"

    override fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        try {
            val simpleEconomy = SimpleEconomyHook(liteEco)
            runBlocking {
                simpleEconomy.getAccounts().forEach { el ->
                    val uuid = UUID.fromString(el.key)
                    val player = Bukkit.getOfflinePlayer(uuid)
                    val balance = el.value.toBigDecimal()
                    if (liteEco.api.createOrUpdateAccount(uuid, player.name.toString(), currency, balance)) {
                        balances += balance
                        converted++
                    }
                }
            }
        } catch (e : Exception) {
            liteEco.logger.severe("SimpleEconomy import error: ${e.message ?: e.localizedMessage}")
        }

        return EconomyImportResults(converted, balances)
    }

}