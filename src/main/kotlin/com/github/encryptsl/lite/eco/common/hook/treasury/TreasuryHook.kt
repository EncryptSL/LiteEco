package com.github.encryptsl.lite.eco.common.hook.treasury

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.hook.HookListener
import me.lokka30.treasury.api.common.service.Service
import me.lokka30.treasury.api.common.service.ServiceRegistry
import me.lokka30.treasury.api.economy.EconomyProvider

class TreasuryHook(
    private val liteEco: LiteEco
) : HookListener(
    PLUGIN_NAME,
    "Support of this economy api is very experimental and don't recommended to use !"
) {

    companion object {
        const val PLUGIN_NAME = "Treasury"
    }

    override fun canRegister(): Boolean {
        return !registered && liteEco.pluginManager.isPluginEnabled(PLUGIN_NAME)
    }

    override fun register() {
        ServiceRegistry.INSTANCE.registerService(
            EconomyProvider::class.java,
            TreasuryEconomyAPI(liteEco),
            "LiteEco",
            me.lokka30.treasury.api.common.service.ServicePriority.NORMAL
        )

        registered = true
    }

    override fun unregister() {
        liteEco.componentLogger.info(ModernText.miniModernText("Unregistering service."))

        var service: Service<EconomyProvider>? = null
        for (otherService in ServiceRegistry.INSTANCE.allServicesFor(EconomyProvider::class.java)) {
            if (otherService.registrarName() != liteEco.description.name) continue
            service = otherService
            break
        }

        if (service == null) {
            liteEco.componentLogger.info(ModernText.miniModernText("Can't unregister service: is already unregistered."))
            return
        }

        ServiceRegistry.INSTANCE.unregister(EconomyProvider::class.java, service)

        registered = false

        liteEco.componentLogger.info(ModernText.miniModernText("Unregistered service successfully."))
    }
}