package com.github.encryptsl.lite.eco.common.manager.importer

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.BetterEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.CraftConomy3Importer
import com.github.encryptsl.lite.eco.common.manager.importer.economies.EssentialsXImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.ScruffyBoyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.SimpleEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.TheosisEconomyImporter
import org.bukkit.Bukkit

class ImportEconomy(private val liteEco: LiteEco) {

    private val offlinePlayers = Bukkit.getOfflinePlayers()
    private val importers = mutableMapOf<String, EconomyImporter>()

    init {
        registerImporter(EssentialsXImporter())
        registerImporter(BetterEconomyImporter())
        registerImporter(ScruffyBoyImporter())
        registerImporter(CraftConomy3Importer())
        registerImporter(SimpleEconomyImporter())
        registerImporter(TheosisEconomyImporter())
    }

    private fun registerImporter(importer: EconomyImporter) {
        importers[importer.name] = importer
    }

    fun import(pluginName: String, currency: String): EconomyImportResults {
        val importer = importers[pluginName] ?: throw IllegalArgumentException("Importer $pluginName not found")
        return importer.import(currency, liteEco, offlinePlayers)
    }

    fun getAvailableImporters(): Set<String> = importers.keys
}