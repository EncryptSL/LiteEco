package com.github.encryptsl.lite.eco.common.extensions

import com.github.encryptsl.lite.eco.LiteEco
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

suspend fun <T> io(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)

suspend fun <T> mainThread(liteEco: LiteEco, block: suspend CoroutineScope.() -> T): T =
    withContext(liteEco.bukkitDispatchers.main, block)

suspend fun CommandSender.safeSendMessage(liteEco: LiteEco, component: Component) {
    mainThread(liteEco) {
        this@safeSendMessage.sendMessage(component)
    }
}