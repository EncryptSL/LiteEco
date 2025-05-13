package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.utils.ConvertEconomy.Economies
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

class ConvertEconomyParser : ArgumentParser<Source, Economies> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<Economies> {
        val input = commandInput.peekString()

        try {
            commandInput.readString()
            return ArgumentParseResult.success(Economies.valueOf(input))
        } catch (_ : IllegalArgumentException) {
            return ArgumentParseResult.failure(Exception(LiteEco.instance.locale.getMessage("messages.parser.error.convert_fail")))
        }
    }

    override fun suggestionProvider(): SuggestionProvider<Source> {
        return SuggestionProvider.blocking { _, _ ->
            Economies.entries.map { Suggestion.suggestion(it.name) }
        }
    }
}