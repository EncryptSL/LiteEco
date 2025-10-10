package com.github.encryptsl.lite.eco.api.interfaces

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.importer.EconomyImportResults
import org.bukkit.OfflinePlayer

/**
 * Interface defining a contract for an economy data importer.
 *
 * An implementation of this interface is responsible for reading balance data
 * from an external economy system and converting/inserting it into the
 * target economy system (presumably LiteEco).
 */
interface EconomyImporter {

    /**
     * A unique and human-readable name for this specific importer (e.g., "EssentialsX Importer").
     *
     * This name is typically used for logging, configuration, or user display.
     */
    val name: String

    /**
     * Executes the balance import process from the external economy source.
     *
     * @param currency The key or identifier of the currency to be imported.
     * @param liteEco An instance of the target economy system's main class, used for API calls to deposit/set balances.
     * @param offlinePlayers An array of all [OfflinePlayer]s whose data should be checked and imported.
     * @return An [EconomyImportResults] object detailing the outcome of the import operation.
     */
    fun import(currency: String, liteEco: LiteEco, offlinePlayers: Array<OfflinePlayer>): EconomyImportResults
}