package com.github.encryptsl.lite.eco.utils

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

class BukkitDispatchers(private val plugin: Plugin) {
    val main: CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }
}