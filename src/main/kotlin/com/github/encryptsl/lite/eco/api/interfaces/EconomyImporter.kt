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
     * Executes the balance import process from an external economy source.
     *
     * @param currencyForImport The key or identifier of the currency to be imported from the source system.
     * @param liteEcoCurrency The target currency identifier within the LiteEco system where data will be stored.
     * @param liteEco An instance of the target economy system's main class, used for API calls to deposit or set balances.
     * @param offlinePlayers An array of all [OfflinePlayer]s whose data should be checked and imported.
     * @return An [EconomyImportResults] object detailing the outcome and statistics of the import operation.
     */
    suspend fun import(
        currencyForImport: String?,
        liteEcoCurrency: String,
        liteEco: LiteEco,
        offlinePlayers: Array<OfflinePlayer>
    ): EconomyImportResults
}