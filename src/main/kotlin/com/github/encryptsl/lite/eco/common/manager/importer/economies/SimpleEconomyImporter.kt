package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.hook.economy.simpleeconomy.SimpleEconomyHook
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class SimpleEconomyImporter(
    private val liteEco: LiteEco
) : BaseEconomyImporter() {

    override val name: String = "SimpleEconomy"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val simpleEconomy = SimpleEconomyHook(liteEco)
        val accounts = simpleEconomy.getAccounts()

        return executeImport(pluginName, liteEco, intoCurrency, accounts.asIterable()) { entry ->
            val uuid = UUID.fromString(entry.key)
            val player = Bukkit.getOfflinePlayer(uuid)
            val name = player.name ?: "Unknown"
            val balance = entry.value.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, name, balance)
        }
    }

}