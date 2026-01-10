package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.LangParser
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

class EcoConfigCmd(
    private val liteEco: LiteEco,
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val configSubCommand = base.literal("config", "c")

        commandManager.command(
            configSubCommand.literal("lang")
                .permission("lite.eco.admin.lang")
                .required(
                    commandManager
                        .componentBuilder(String::class.java, "isoKey")
                        .parser(LangParser(liteEco))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val lang: String = ctx.get("isoKey")

                    if (liteEco.locale.setLocale(lang)) {
                        sender.sendMessage(
                            liteEco.locale
                                .translation("messages.admin.translation_switch", Placeholder.parsed("locale", lang))
                        )
                    }
                })
        commandManager.command(
            configSubCommand.literal("reload")
                .permission("lite.eco.admin.reload").handler { ctx ->
                    liteEco.reloadConfig()
                    liteEco.locale.reloadCurrentLocale()
                    ctx.sender().source().sendMessage(liteEco.locale.translation("messages.admin.config_reload"))
                    liteEco.componentLogger.info(ModernText.miniModernText("✅ config.yml & locale reloaded."))
                })
    }
}