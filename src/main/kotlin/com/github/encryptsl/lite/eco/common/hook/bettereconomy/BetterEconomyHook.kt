package com.github.encryptsl.lite.eco.common.hook.bettereconomy

import me.hsgamer.bettereconomy.BetterEconomy
import me.hsgamer.bettereconomy.api.EconomyHandler
import me.hsgamer.bettereconomy.provider.EconomyHandlerProvider
import java.util.*

class BetterEconomyHook {

    private lateinit var economyHandler: EconomyHandler

    fun getBetterEconomy(): Boolean {
        return try {
            Class.forName("me.hsgamer.bettereconomy.BetterEconomy")
            economyHandler = BetterEconomy().get(EconomyHandlerProvider::class.java).economyHandler
            true
        } catch (e : ClassNotFoundException) {
            false
        }
    }

    fun getBalance(uuid: UUID): Double {
        if (!getBetterEconomy())
            throw Exception("Plugin BetterEconomy missing !")
        return economyHandler.get(uuid)
    }
}