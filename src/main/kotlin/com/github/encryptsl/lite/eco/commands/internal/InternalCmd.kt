package com.github.encryptsl.lite.eco.commands.internal

import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

interface InternalCmd {
    fun execute(commandManager: PaperCommandManager<Source>)
}