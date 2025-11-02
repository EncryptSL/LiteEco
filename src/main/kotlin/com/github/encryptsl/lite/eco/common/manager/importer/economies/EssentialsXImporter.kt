package com.github.encryptsl.lite.eco.common.manager.importer.economies

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.interfaces.EconomyImporter
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import kotlinx.coroutines.runBlocking
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.math.BigDecimal

class EssentialsXImporter : EconomyImporter {

    override val name: String = "EssentialsX"

    override fun import(
        currency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults {
        var converted = 0
        var balances = BigDecimal.ZERO

        runBlocking {
            for (p in offlinePlayers) {
                val playerFile = File("plugins/Essentials/userdata/", "${p.uniqueId}.yml")
                if (playerFile.exists()) {
                    val config = YamlConfiguration.loadConfiguration(playerFile)
                    val balance = config.getString("money")?.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (liteEco.api.createOrUpdateAccount(p.uniqueId, p.name.toString(), currency, balance)) {
                        converted++
                        balances += balance
                    }
                }
            }
        }

        return EconomyImportResults(converted, balances)
    }
}