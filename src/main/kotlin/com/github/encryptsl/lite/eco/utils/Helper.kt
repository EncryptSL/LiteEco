@file:Suppress("DEPRECATION")
package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.account.PlayerAccount
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import com.github.encryptsl.lite.eco.common.database.entity.UserEntity
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.convertInstant
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologPageResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.ExperimentalTime

class Helper(private val liteEco: LiteEco) {

    @OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
    internal fun TransactionContextEntity.toComponent(translation: String): Component {
        val locale = LiteEco.instance.locale
        val formatter = LiteEco.instance.currencyImpl

        return locale.translation(translation, TagResolver.resolver(
            Placeholder.unparsed("action", type.name), // type je nyní Enum v entitě
            Placeholder.unparsed("sender", sender),
            Placeholder.unparsed("target", target),
            Placeholder.unparsed("currency", currency),
            Placeholder.unparsed("previous_balance", formatter.fullFormatting(previousBalance, currency)),
            Placeholder.unparsed("new_balance", formatter.fullFormatting(newBalance, currency)),
            Placeholder.unparsed("timestamp", convertInstant(timestamp))
        ))
    }

    internal fun inspectCache(sender: CommandSender, uuid: UUID) {
        val account = PlayerAccount.cache[uuid]

        sender.sendMessage("§8--- §bInspecting cache for: §f$uuid §8---")

        if (account == null || account.balances.isEmpty()) {
            sender.sendMessage("§cCache is empty for this player.")
            return
        }

        val statusColor = if (account.isSuccessfullyLoaded) "§aVALID (Loaded)" else "§cINVALID (Not Loaded)"
        sender.sendMessage("§7Data Integrity: $statusColor")

        account.balances.forEach { (currency, amount) ->
            sender.sendMessage("§7Currency: §e$currency §7| Amount: §a$amount")
        }

        val isOnline = Bukkit.getPlayer(uuid) != null
        sender.sendMessage("§7Player Status: " + if (isOnline) "§aONLINE §8(Ignored by Janitor)" else "§cOFFLINE §8(Will be processed by Janitor)")
    }

    internal fun executeJanitorTest(player: Player) {
        try {
            val vault = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider

            // STEP 1: Enable failMode
            DatabaseEcoModel.debugFailMode = true
            player.sendMessage("§8[§bLiteEco-Test§8] §cFailMode ACTIVATED.")

            // STEP 2: Vault Transaction
            vault?.depositPlayer(player, 500.0)
            player.sendMessage("§8[§bLiteEco-Test§8] §7Vault: Deposited 500.0.")

            // STEP 3: Instructions
            player.sendMessage("§8[§bLiteEco-Test§8] §ePlease disconnect from the server now.")
            player.sendMessage("§7You should see a sync error in the console, but data MUST remain in the cache.")
        } catch (e : Exception) {
            liteEco.logger.error(e.message, e)
        }
    }

    internal fun forceJanitorSync(sender: CommandSender) {
        sender.sendMessage("§7Forcing Janitor execution...")

        val task = Runnable {
            val offlineUUIDs = PlayerAccount.cache.keys.filter { uuid ->
                Bukkit.getPlayer(uuid) == null
            }

            if (offlineUUIDs.isEmpty()) {
                sender.sendMessage("§eJanitor: No data to synchronize (everyone is online or cache is empty).")
                return@Runnable
            }

            offlineUUIDs.forEach { uuid ->
                PlayerAccount.syncAccount(uuid)
            }

            sender.sendMessage("§aJanitor completed emergency synchronization for §e${offlineUUIDs.size} §aaccounts.")
        }

        if (!LiteEco.isFolia()) {
            Bukkit.getScheduler().runTaskAsynchronously(liteEco, task)
        } else {
            Bukkit.getAsyncScheduler().runNow(liteEco, {
                task.run()
            })
        }
    }

    internal fun executeStressTest(player: Player, amount: Double, iterations: Int) {
        try {
            val vault = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider

            player.sendMessage("§7Starting stress-test: §e$iterations §7iterations...")

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

                player.sendMessage("§aStress-test finished in §e${duration}ms.")
                player.sendMessage("§7Balance should be the same as at the start.")
                player.sendMessage("§7Current balance: §e${vault?.getBalance(player)}")
            }

            if (!LiteEco.isFolia()) {
                Bukkit.getScheduler().runTaskAsynchronously(LiteEco.instance, task)
            } else {
                Bukkit.getAsyncScheduler().runNow(LiteEco.instance, {task.run()})
            }
        } catch (e : Exception) {
            liteEco.logger.error(e.message, e)
        }
    }
}