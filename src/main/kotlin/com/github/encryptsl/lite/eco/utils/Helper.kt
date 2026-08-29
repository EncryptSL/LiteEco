@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.account.AccountCache
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.convertInstant
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologPageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*

class Helper(private val liteEco: LiteEco) {

    suspend fun validateLog(parameter: String, page: Int): MonologPageResult {
        val pageSize = 10
        val (logs, totalPages) = liteEco.loggerModel.getLogPage(parameter, page, pageSize)

        return MonologPageResult(
            components = logs.map { it.toComponent("messages.monolog.formatting") },
            totalPages = totalPages
        )
    }

    fun getTopBalancesFormatted(currency: String): List<Component> {
        return liteEco.api.getTopBalance(currency).toList().positionIndexed { index, pair ->
                liteEco.locale.translation("messages.balance.top_format", TagResolver.resolver(
                    Placeholder.parsed("position", index.toString()),
                    Placeholder.parsed("player", pair.first),
                    Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(pair.second, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, pair.second))
                ))
        }
    }

    fun getComponentBal(user: UserEntity, currency: String): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", user.userName),
            Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(user.money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, user.money))
        )
    }

    internal fun TransactionContextEntity.toComponent(translation: String): Component {
        val locale = LiteEco.instance.locale
        val formatter = LiteEco.instance.currencyImpl

        val difference = newBalance.subtract(previousBalance)

        val diffStatus = difference.compareTo(BigDecimal.ZERO)
        val diffPrefix = if (diffStatus >= 0) "+" else "-"
        val diffColor = if (diffStatus >= 0) "<green>" else "<red>"

        val symbols = when(type.name.uppercase()) {
            "DEPOSIT" -> "<green>➕</green>"
            "WITHDRAW" -> "<red>➖</red>"
            "SET" -> "<blue><b>=</b></blue>"
            else -> "•"
        }

        val hoverWithSymbols = "<hover:show_text:'${type.name}'>$symbols</hover>"

        val formattedDiff = formatter.fullFormatting(difference.abs(), currency)

        return locale.translation(translation, TagResolver.resolver(
            Placeholder.parsed("symbol", hoverWithSymbols),
            Placeholder.unparsed("action", type.name),
            Placeholder.unparsed("sender", sender),
            Placeholder.unparsed("target", target),
            Placeholder.unparsed("currency", currency),
            Placeholder.unparsed("previous_balance", formatter.fullFormatting(previousBalance, currency)),
            Placeholder.unparsed("new_balance", formatter.fullFormatting(newBalance, currency)),
            Placeholder.parsed("diff", "$diffColor$diffPrefix$formattedDiff"),
            Placeholder.unparsed("timestamp", convertInstant(timestamp))
        ))
    }

    internal fun inspectCache(sender: CommandSender, uuid: UUID) {
        val account = AccountCache.cache[uuid]

        sender.sendMessage(ModernText.miniModernText("<dark_gray>--- <aqua>Inspecting cache for: <white>$uuid <dark_gray>---"))

        if (account == null || account.balances.isEmpty()) {
            sender.sendMessage(ModernText.miniModernText("<red>Cache is empty for this player."))
            return
        }

        val statusColor = if (account.isSuccessfullyLoaded) "<green>VALID (Loaded)</green>" else "<red>INVALID (Not Loaded)</red>"
        sender.sendMessage(ModernText.miniModernText("<gray>Data Integrity: $statusColor"))

        account.balances.forEach { (currency, amount) ->
            sender.sendMessage(ModernText.miniModernText("<gray>Currency: <yellow>$currency</yellow> | Amount: <green>$amount</green></gray>"))
        }

        val isOnline = Bukkit.getPlayer(uuid) != null
        val playerStatus = if (isOnline) {
            "<green>ONLINE <dark_gray>(Ignored by Janitor)</dark_gray></green>"
        } else {
            "<red>OFFLINE <dark_gray>(Will be processed by Janitor)</dark_gray></red>"
        }

        sender.sendMessage(ModernText.miniModernText("<gray>Player Status: $playerStatus"))
    }

    internal fun executeJanitorTest(player: Player) {
        try {
            val vault = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider

            // STEP 1: Enable failMode
            DatabaseEcoModel.debugFailMode = true
            player.sendMessage(ModernText.miniModernText("<dark_gray>[<cyan>LiteEco-Test</cyan>] <red>FailMode ACTIVATED.</dark_gray>"))

            // STEP 2: Vault Transaction
            vault?.depositPlayer(player, 500.0)
            player.sendMessage(ModernText.miniModernText("<dark_gray>[<cyan>LiteEco-Test</cyan>] <gray>Vault: Deposited 500.0.</gray></dark_gray>"))

            // STEP 3: Instructions
            player.sendMessage(ModernText.miniModernText("<dark_gray>[<cyan>LiteEco-Test</cyan>] <yellow>Please disconnect from the server now.</yellow></dark_gray>"))
            player.sendMessage(ModernText.miniModernText("<gray>You should see a sync error in the console, but data MUST remain in the cache.</gray>"))
        } catch (e: Exception) {
            liteEco.logger.error(e.message, e)
        }
    }

    internal fun forceJanitorSync(sender: CommandSender) {
        sender.sendMessage(ModernText.miniModernText("<gray>Forcing Janitor execution...</gray>"))

        liteEco.pluginScope.launch(Dispatchers.IO) {
            val offlineUUIDs = AccountCache.cache.keys.filter { uuid ->
                !AccountCache.isPlayerOnline(uuid)
            }

            if (offlineUUIDs.isEmpty()) {
                sender.sendMessage(ModernText.miniModernText("<yellow>Janitor: No data to synchronize (everyone is online or cache is empty).</yellow>"))
                return@launch
            }

            var savedCount = 0

            for (uuid in offlineUUIDs) {
                val isSaved = AccountCache.withLock(uuid) {
                    if (!AccountCache.isPlayerOnline(uuid)) {
                        AccountCache.sync(uuid, shouldUnload = true)
                    } else false
                }
                if (isSaved) savedCount++
            }

            when {
                savedCount == offlineUUIDs.size -> {
                    sender.sendMessage(ModernText.miniModernText("<green>Janitor completed emergency synchronization for <yellow>$savedCount</yellow> accounts.</green>"))
                }
                savedCount > 0 -> {
                    sender.sendMessage(ModernText.miniModernText("<yellow>Janitor partially synchronized <green>$savedCount</green>/<red>${offlineUUIDs.size}</red> accounts. Check logs for errors.</yellow>"))
                }
                else -> {
                    sender.sendMessage(ModernText.miniModernText("<red>Janitor execution failed: 0/${offlineUUIDs.size} accounts were synchronized. (FailMode active or DB error).</red>"))
                }
            }
        }
    }

    internal fun executeStressTest(player: Player, amount: Double, iterations: Int) {
        try {
            val vault = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider

            player.sendMessage(ModernText.miniModernText("<gray>Starting stress-test: <yellow>$iterations</yellow> iterations...</gray>"))

            val task = Runnable {
                val startTime = System.currentTimeMillis()

                for (i in 1..iterations) {
                    val t1 = Thread { vault?.depositPlayer(player, amount) }
                    val t2 = Thread { vault?.withdrawPlayer(player, amount) }

                    t1.start()
                    t2.start()

                    t1.join()
                    t2.join()
                }

                val duration = System.currentTimeMillis() - startTime

                player.sendMessage(ModernText.miniModernText("<green>Stress-test finished in <yellow>${duration}ms</yellow>.</green>"))
                player.sendMessage(ModernText.miniModernText("<gray>Balance should be the same as at the start.</gray>"))
                player.sendMessage(ModernText.miniModernText("<gray>Current balance: <yellow>${vault?.getBalance(player)}</yellow></gray>"))
            }

            liteEco.schedulerHelper.runAsyncNow(task)
        } catch (e: Exception) {
            liteEco.logger.error(e.message, e)
        }
    }
}