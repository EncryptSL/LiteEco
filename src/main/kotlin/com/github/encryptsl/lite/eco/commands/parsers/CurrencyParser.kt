package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import org.bukkit.command.CommandSender
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider


class CurrencyParser : ArgumentParser<Source, String> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<String> {
        val input = commandInput.peekString()

        if (!LiteEco.instance.currencyImpl.getCurrencyNameExist(input)) {
            val message = LiteEco.instance.locale.getMessage("messages.parser.error.currency_not_exist")
            return ArgumentParseResult.failure(Exception(String.format(message, input)))
        }
        commandInput.readString()
        return ArgumentParseResult.success(input)
    }

    override fun suggestionProvider(): SuggestionProvider<Source> {
        return SuggestionProvider.blocking { s, i ->
            LiteEco.instance.currencyImpl.getCurrenciesKeys().filter {
                val sender: CommandSender = s.sender().source()
                sender.hasPermission("lite.eco.balance.$it") || sender.hasPermission("lite.eco.balance.*")
            }.map { Suggestion.suggestion(it) }
        }
    }
}