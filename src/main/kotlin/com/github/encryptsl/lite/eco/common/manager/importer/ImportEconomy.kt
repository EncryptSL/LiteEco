package com.github.encryptsl.lite.eco.common.manager.importer

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.economies.*

class ImportEconomy(private val liteEco: LiteEco) {

    internal val importers = mutableMapOf<String, EconomyImporter>()

    init {
        registerImporter(EssentialsXImporter(liteEco))
        registerImporter(PlayerPointsImporter(liteEco))
        registerImporter(BetterEconomyImporter(liteEco))
        registerImporter(ScruffyBoyImporter(liteEco))
        registerImporter(EzEconomyImporter(liteEco))
        registerImporter(SimpleEconomyImporter(liteEco))
        registerImporter(TheosisEconomyImporter(liteEco))
    }

    private fun registerImporter(importer: EconomyImporter) {
        importers[importer.name] = importer
    }

    suspend fun import(pluginName: String, intoCurrency: String, fromCurrency: String?): EconomyImportResults {
        if (liteEco.databaseEcoModel.getUUIDNameMap(intoCurrency).isNotEmpty())
            throw Exception("lite_eco_$intoCurrency table is not empty !")

        val importer = importers[pluginName] ?: throw IllegalArgumentException("Importer $pluginName not found")

        if (importer.isMultiCurrency && fromCurrency == null) {
            throw IllegalArgumentException("The plugin '$pluginName' requires a source currency. Usage: /eco database import $pluginName $intoCurrency --from <currency>")
        }

        return importer.import(pluginName, intoCurrency, fromCurrency)
    }
}