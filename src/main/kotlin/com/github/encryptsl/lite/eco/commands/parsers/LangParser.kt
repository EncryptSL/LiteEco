package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.SuggestionProvider

class LangParser(
    private val liteEco: LiteEco
) : ArgumentParser<Source, String> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<String> {
        val input = commandInput.readString()

        if (liteEco.locale.isLocaleAvailable(input)) {
            return ArgumentParseResult.success(input)
        } else {
            val message = liteEco.locale.getMessage("messages.parser.error.language_not_exist")
            return ArgumentParseResult.failure(Exception(String.format(message, input)))
        }
    }

    override fun suggestionProvider(): SuggestionProvider<Source> {
        return SuggestionProvider.suggestingStrings(liteEco.locale.availableLocales())
    }
}