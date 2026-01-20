package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.importer.BaseEconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class EssentialsXImporter(
    private val liteEco: LiteEco
) : BaseEconomyImporter() {

    override val name: String = "EssentialsX"

    override suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults {
        val userDataFolder = File("plugins/Essentials/userdata/")
        if (!userDataFolder.exists()) return EconomyImportResults(0, BigDecimal.ZERO)

        return executeImport(pluginName, liteEco, intoCurrency, offlinePlayers.asIterable()) { player ->
            val playerFile = File(userDataFolder, "${player.uniqueId}.yml")
            if (!playerFile.exists()) return@executeImport null

            val config = YamlConfiguration.loadConfiguration(playerFile)
            val balance = config.getString("money")?.toBigDecimalOrNull() ?: BigDecimal.ZERO

            if (balance <= BigDecimal.ZERO) return@executeImport null

            Triple(player.uniqueId, player.name ?: "Unknown", balance)
        }
    }

}