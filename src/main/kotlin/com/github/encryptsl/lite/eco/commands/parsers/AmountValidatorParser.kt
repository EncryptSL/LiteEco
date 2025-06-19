package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.CheckLevel
import com.github.encryptsl.lite.eco.common.extensions.isApproachingZero
import com.github.encryptsl.lite.eco.common.extensions.isNegative
import com.github.encryptsl.lite.eco.common.extensions.toValidDecimal
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.math.BigDecimal

class AmountValidatorParser(
    val level: CheckLevel = CheckLevel.FULL
) : ArgumentParser<Source, BigDecimal> {
    override fun parse(
        commandContext: CommandContext<Source>,
        commandInput: CommandInput
    ): ArgumentParseResult<BigDecimal> {
        val amountStr = commandInput.readString()

        val amount = amountStr.toValidDecimal() ?:
            return ArgumentParseResult.failure(Exception(LiteEco.instance.locale.getMessage("messages.parser.error.format_amount")))

        val isInvalid = when (level) {
            CheckLevel.ONLY_NEGATIVE -> amount.isNegative()
            CheckLevel.FULL -> amount.isApproachingZero()
        }

        if (isInvalid) {
            return ArgumentParseResult.failure(Exception(LiteEco.instance.locale.getMessage("messages.parser.error.negative_amount")))
        }

        return ArgumentParseResult.success(amount)
    }

    /**
     * I don't know how organize list, incendo cloud everytime keep order list by alphabet
     */
    override fun suggestionProvider(): SuggestionProvider<Source> {
        return SuggestionProvider.blockingStrings { _, input ->
            val raw = input.lastRemainingToken().filter { it.isDigit() }

            if (raw.isBlank()) return@blockingStrings emptyList()

            val base = raw.toLongOrNull() ?: return@blockingStrings emptyList()

            val multipliers = listOf(1L, 10, 100, 1_000, 10_000, 100_000, 1_000_000)

            multipliers.map { multiplier ->
                val value = base * multiplier
                Suggestion.suggestion(value.toString()).toString()
            }
        }

        /*
        return SuggestionProvider.blockingStrings { _, input ->
            val rawInput = input.peekString().trim().lowercase()
            val base = rawInput.filter { it.isDigit() || it == '.' }

            if (base.isEmpty()) return@blockingStrings emptyList()

            val units = listOf("k", "m", "b", "t", "q")

            buildList {
                if (base.startsWith(rawInput) || rawInput.startsWith(base)) {
                    add(base)
                }

                units.forEach { unit ->
                    val candidate = base + unit
                    if (candidate.startsWith(rawInput)) {
                        add(candidate)
                    }
                }
            }
        }*/
    }
}