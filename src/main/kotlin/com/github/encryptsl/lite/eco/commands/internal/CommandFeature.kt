package com.github.encryptsl.lite.eco.commands.internal

import org.incendo.cloud.Command
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

/**
 * Represents a modular command feature that can be registered to a command manager.
 * This interface allows splitting a large command tree into smaller, maintainable classes.
 */
interface CommandFeature {

    /**
     * The default description for commands within this feature.
     */
    val DESCRIPTION: String
        get() = "Provided plugin by LiteEco"

    /**
     * Registers the command or sub-commands to the provided [commandManager].
     *
     * @param commandManager The Paper command manager instance.
     * @param base The base command builder (e.g., the "/eco" or "/money" builder).
     */
    fun register(commandManager: PaperCommandManager<Source>, base: Command.Builder<Source>)
}