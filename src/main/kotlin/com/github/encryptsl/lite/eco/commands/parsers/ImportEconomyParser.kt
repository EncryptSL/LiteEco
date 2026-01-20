package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.importer.ImportEconomy
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider

class ImportEconomyParser(private val importer: ImportEconomy) : ArgumentParser<Source, String> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<String> {
        val input = commandInput.readString()
        val keys = importer.importers.keys

        if (keys.contains(input)) {
            return ArgumentParseResult.success(input)
        }

        return ArgumentParseResult.failure(Exception(LiteEco.instance.locale.getMessage("messages.parser.error.convert_fail")))
    }

    override fun suggestionProvider(): SuggestionProvider<Source> {
        val keys = importer.importers.keys
        return SuggestionProvider.suggesting(keys.map { Suggestion.suggestion(it) })
    }
}