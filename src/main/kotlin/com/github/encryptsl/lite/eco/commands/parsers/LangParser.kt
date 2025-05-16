package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.config.Locales
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

class LangParser : ArgumentParser<Source, Locales.LangKey> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<Locales.LangKey> {
        val input = commandInput.peekString()

        try {
            commandInput.readString()
            return ArgumentParseResult.success(Locales.LangKey.valueOf(input))
        } catch (_ : IllegalArgumentException) {
            val message = LiteEco.instance.locale.getMessage("messages.parser.error.language_not_exist")
            return ArgumentParseResult.failure(Exception(String.format(message, input)))
        }
    }

    override fun suggestionProvider(): SuggestionProvider<Source> {
        return SuggestionProvider.suggesting(Locales.LangKey.entries.map { Suggestion.suggestion(it.name) })
    }
}