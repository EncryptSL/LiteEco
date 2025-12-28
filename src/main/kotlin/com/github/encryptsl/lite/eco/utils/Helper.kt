package com.github.encryptsl.lite.eco.utils

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.PlayerAccount
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.User
import com.github.encryptsl.lite.eco.common.database.models.DatabaseEcoModel
import com.github.encryptsl.lite.eco.common.extensions.positionIndexed
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
    suspend fun validateLog(parameter: String): List<Component> {
        val log = liteEco.loggerModel.getLog()
            .let { if (parameter != "all") it.filter { p -> p.target == parameter } else it }

        return log.map { el ->
            liteEco.loggerModel.message("messages.monolog.formatting",
                TypeLogger.valueOf(el.action),
                el.sender,
                el.target,
                el.currency,
                el.previousBalance,
                el.newBalance,
                el.timestamp
            )
        }
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

    fun getComponentBal(user: User, currency: String): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", user.userName),
            Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(user.money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, user.money))
        )
    }

    internal fun inspectCache(sender: CommandSender, uuid: UUID) {
        val userCache = PlayerAccount.cache[uuid]

        sender.sendMessage("§8--- §bInspecting cache for: §f$uuid §8---")

        if (userCache.isNullOrEmpty() || userCache.isEmpty()) {
            sender.sendMessage("§cCache is empty for this player.")
            return
        }

        userCache.forEach { (currency, amount) ->
            sender.sendMessage("§7Currency: §e$currency §7| Amount: §a$amount")
        }

        val isOnline = Bukkit.getPlayer(uuid) != null
        sender.sendMessage("§7Player Status: " + if (isOnline) "§aONLINE §8(Ignored by Janitor)" else "§cOFFLINE §8(Will be processed by Janitor)")
    }

    internal fun executeJanitorTest(player: Player) {
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
    }

    internal fun forceJanitorSync(sender: CommandSender) {
        sender.sendMessage("§7Forcing Janitor execution...")

        Bukkit.getScheduler().runTaskAsynchronously(LiteEco.instance, Runnable {
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
        })
    }

    internal fun executeStressTest(player: Player, amount: Double, iterations: Int) {
        val vault = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
        if (vault == null) {
            player.sendMessage("§cError: Vault was not found!")
            return
        }

        player.sendMessage("§7Starting stress-test: §e$iterations §7iterations...")

        Bukkit.getScheduler().runTaskAsynchronously(LiteEco.instance, Runnable {
            val startTime = System.currentTimeMillis()

            for (i in 1..iterations) {
                val t1 = Thread { vault.depositPlayer(player, amount) }
                val t2 = Thread { vault.withdrawPlayer(player, amount) }

                t1.start()
                t2.start()

                t1.join()
                t2.join()
            }

            val duration = System.currentTimeMillis() - startTime

            Bukkit.getScheduler().runTask(LiteEco.instance, Runnable {
                player.sendMessage("§aStress-test finished in §e${duration}ms.")
                player.sendMessage("§7Balance should be the same as at the start.")
                player.sendMessage("§7Current balance: §e${vault.getBalance(player)}")
            })
        })
    }
}