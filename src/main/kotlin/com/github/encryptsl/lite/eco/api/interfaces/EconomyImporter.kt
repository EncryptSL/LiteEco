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

    val isMultiCurrency: Boolean

    /**
     * Executes the balance import process from an external economy source.
     *
     * @param pluginName The name of the source economy plugin (e.g., "EssentialsX", "EzEconomy").
     * @param intoCurrency The target currency identifier within LiteEco where the balances will be deposited.
     * @param fromCurrency The specific source currency identifier from the external plugin.
     * This is optional and only required if the source plugin supports multiple currencies.
     * @return An [EconomyImportResults] object detailing the number of migrated accounts and the total balance amount.
     * @throws IllegalArgumentException if the importer is not found or if a required source currency is missing.
     * @throws Exception if the target database table for [intoCurrency] is not empty.
     */
    suspend fun import(
        pluginName: String,
        intoCurrency: String,
        fromCurrency: String?
    ): EconomyImportResults
}