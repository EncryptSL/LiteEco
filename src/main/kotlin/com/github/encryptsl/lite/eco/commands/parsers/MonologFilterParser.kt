package com.github.encryptsl.lite.eco.commands.parsers

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologFilter
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.suggestion.SuggestionProvider
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

class MonologFilterParser<C> : SuggestionProvider<C> {

    override fun suggestionsFuture(
        context: CommandContext<C>,
        input: CommandInput
    ): CompletableFuture<Iterable<Suggestion>> {
        val currentInput = try {
            input.peekString()
        } catch (_: Exception) {
            ""
        }

        val isQuoted = currentInput.startsWith("\"") || currentInput.startsWith("'")

        val tokenToComplete = currentInput
            .removePrefix("\"")
            .removePrefix("'")
            .split(Regex("[,\\s]+"))
            .lastOrNull()
            .orEmpty()
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")

        val baseSuggestions = listOf("\"t:\"", "\"s:\"", "\"u:\"", "\"c:\"", "\"a:\"", "\"am:\"")

        val suggestions = when {
            tokenToComplete.startsWith("c:", ignoreCase = true) -> {
                val prefix = tokenToComplete.substringBefore(":")
                val sender = extractSender(context.sender())

                val currencies = if (sender is ConsoleCommandSender || sender == null) {
                    LiteEco.instance.currencyImpl.getCurrenciesKeys()
                } else {
                    LiteEco.instance.currencyImpl.getCurrenciesKeys().filter {
                        sender.hasPermission("lite.eco.balance.$it") || sender.hasPermission("lite.eco.balance.*")
                    }
                }

                currencies.map { "$prefix:$it" }
            }
            tokenToComplete.startsWith("a:", ignoreCase = true) -> {
                val prefix = tokenToComplete.substringBefore(":")
                listOf("DEPOSIT", "WITHDRAW", "TRANSFER", "SET").map { "$prefix:$it" }
            }
            tokenToComplete.startsWith("u:", ignoreCase = true) ||
                    tokenToComplete.startsWith("s:", ignoreCase = true) ||
                    tokenToComplete.startsWith("t:", ignoreCase = true) -> {
                val prefix = tokenToComplete.substringBefore(":")
                Bukkit.getOnlinePlayers().map { "$prefix:${it.name}" }
            }
            else -> baseSuggestions
        }

        val filteredSuggestions = suggestions
            .filter { it.startsWith(tokenToComplete, ignoreCase = true) }
            .map { suggestionText ->
                val formatted = if (isQuoted && !suggestionText.startsWith("\"")) {
                    "\"$suggestionText\""
                } else {
                    suggestionText
                }
                Suggestion.suggestion(formatted)
            }

        return CompletableFuture.completedFuture(filteredSuggestions)
    }

    private fun extractSender(senderObj: Any?): CommandSender? {
        if (senderObj == null) return null
        if (senderObj is CommandSender) return senderObj
        return try {
            senderObj.javaClass.getMethod("source").invoke(senderObj) as? CommandSender
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun parseFromString(rawInput: String): MonologFilter {
            val cleanedInput = rawInput
                .trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")
                .trim()

            if (cleanedInput.isEmpty()) {
                return MonologFilter()
            }

            var action: String? = null
            var sender: String? = null
            var target: String? = null
            var currency: String? = null
            var minAmount: BigDecimal? = null

            val tokens = cleanedInput.split(Regex("[,\\s]+"))

            for (token in tokens) {
                val trimmedToken = token
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")

                if (trimmedToken.isEmpty()) continue

                val parts = trimmedToken.split(":", limit = 2)
                if (parts.size < 2) continue

                val key = parts[0].trim().lowercase().removeSurrounding("\"").removeSurrounding("'")
                val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")

                when (key) {
                    "t" -> target = value
                    "s", "u" -> sender = value
                    "c" -> currency = value
                    "a" -> action = value.uppercase()
                    "am" -> minAmount = value.toBigDecimalOrNull()
                }
            }

            return MonologFilter(
                action = action,
                sender = sender,
                target = target,
                currency = currency,
                minAmount = minAmount
            )
        }
    }
}