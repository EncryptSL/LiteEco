package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.OfflinePlayer

interface EconomyImporter {
    val name: String
    fun import(currency: String, liteEco: LiteEco, offlinePlayers: Array<OfflinePlayer>): EconomyImportResults
}