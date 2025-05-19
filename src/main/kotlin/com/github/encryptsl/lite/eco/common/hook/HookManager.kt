package com.github.encryptsl.lite.eco.common.hook

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.hook.bettereconomy.BetterEconomyHook
import com.github.encryptsl.lite.eco.common.hook.craftconomy3.CraftConomyHook
import com.github.encryptsl.lite.eco.common.hook.miniplaceholder.MiniPlaceholdersHook
import com.github.encryptsl.lite.eco.common.hook.placeholderapi.PlaceholderAPIHook
import com.github.encryptsl.lite.eco.common.hook.scruffyboy13.ScruffyboyEconomyHook
import com.github.encryptsl.lite.eco.common.hook.treasury.TreasuryHook
import com.github.encryptsl.lite.eco.common.hook.vault.VaultHook
import kotlin.system.measureTimeMillis

class HookManager(private val liteEco: LiteEco) {

    private val hooks: Set<HookListener> = mutableSetOf(
        BetterEconomyHook(liteEco),
        CraftConomyHook(liteEco),
        ScruffyboyEconomyHook(liteEco),
        MiniPlaceholdersHook(liteEco),
        PlaceholderAPIHook(liteEco),
        TreasuryHook(liteEco),
        VaultHook(liteEco)
    )

    /**
     * Method for disable plugin if is detected unsupported plugin.
     * @param plugins
     */
    fun blockPlugin(vararg plugins: String) {
        for (plugin in plugins) {
            if (liteEco.pluginManager.isPluginEnabled(plugin)) {
                liteEco.componentLogger.error("Please don't use $plugin, because there can be conflict.")
                liteEco.pluginManager.disablePlugin(liteEco)
            }
        }
        liteEco.componentLogger.warn("Blocked plugins are now ${plugins.size}")
    }

    fun registerHooks() {
        val timeTaken = measureTimeMillis {
            liteEco.componentLogger.info("Registering ${hooks.size} hooks...")

            hooks.filter(HookListener::canRegister).forEach {
                it.register()
                liteEco.componentLogger.info("${it.pluginName} found hook ${it.javaClass.simpleName} registered !.")
                liteEco.componentLogger.info(it.description)
            }
        }
        liteEco.componentLogger.info(ModernText.miniModernText("Registered ${hooks.count { it.registered }} of ${hooks.size} hooks took ${timeTaken} ms"))
    }

    fun unregisterHooks() {
        liteEco.componentLogger.info("Unregistering hooks...")
        hooks.filter(HookListener::canRegister).forEach {
            liteEco.componentLogger.info("Unregistering hook ${it.pluginName}.")
            it.unregister()
            liteEco.componentLogger.info("Unregistered hook ${it.pluginName}.")
        }
    }
}