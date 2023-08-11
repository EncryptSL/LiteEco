package encryptsl.cekuj.net.hook.treasury

import encryptsl.cekuj.net.LiteEco
import me.lokka30.treasury.api.economy.EconomyProvider
import me.lokka30.treasury.api.economy.account.Account
import me.lokka30.treasury.api.economy.account.PlayerAccount
import me.lokka30.treasury.api.economy.currency.Currency
import me.lokka30.treasury.api.economy.misc.OptionalEconomyApiFeature
import me.lokka30.treasury.api.economy.response.EconomyException
import me.lokka30.treasury.api.economy.response.EconomyFailureReason
import me.lokka30.treasury.api.economy.response.EconomySubscriber
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.stream.Collectors

class TreasuryEconomyAPI(private val liteEco: LiteEco, private val currency: Currency) : EconomyProvider {

    companion object {
        const val currencyIdentifier = "lite_eco_economy"
    }

    override fun getSupportedOptionalEconomyApiFeatures(): MutableSet<OptionalEconomyApiFeature> {
        return Collections.emptySet()
    }

    override fun hasPlayerAccount(accountId: UUID, subscription: EconomySubscriber<Boolean>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            if (liteEco.api.hasAccount(Bukkit.getOfflinePlayer(accountId))) {
                subscription.succeed(true)
            } else {
                subscription.fail(EconomyException(EconomyFailureReason.ACCOUNT_NOT_FOUND))
            }
        })
    }

    override fun retrievePlayerAccount(accountId: UUID, subscription: EconomySubscriber<PlayerAccount>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            if (liteEco.api.hasAccount(Bukkit.getOfflinePlayer(accountId))) {
                subscription.succeed(TreasuryAccount(liteEco, accountId))
            } else {
                subscription.fail(EconomyException(EconomyFailureReason.ACCOUNT_NOT_FOUND))
            }
        })
    }

    override fun createPlayerAccount(accountId: UUID, subscription: EconomySubscriber<PlayerAccount>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            liteEco.api.createAccount(Bukkit.getOfflinePlayer(accountId), TreasureCurrency(liteEco).getStartingBalance(null).toDouble())
            subscription.succeed(TreasuryAccount(liteEco, accountId))
        })
    }

    override fun retrievePlayerAccountIds(subscription: EconomySubscriber<MutableCollection<UUID>>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            val uuid: List<UUID> = Arrays.stream(Bukkit.getOfflinePlayers()).parallel().map(OfflinePlayer::getUniqueId).collect(Collectors.toList())

            val identifiers: MutableList<UUID> = uuid.parallelStream()
                .filter{ puuid -> liteEco.api.hasAccount(Bukkit.getOfflinePlayer(puuid))}
                .collect(Collectors.toList())

            subscription.succeed(identifiers)
        })
    }

    override fun hasAccount(identifier: String, subscription: EconomySubscriber<Boolean>) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun retrieveAccount(identifier: String, subscription: EconomySubscriber<Account>) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun createAccount(name: String?, identifier: String, subscription: EconomySubscriber<Account>) {
        subscription.fail(EconomyException(EconomyFailureReason.FEATURE_NOT_SUPPORTED))
    }

    override fun retrieveAccountIds(subscription: EconomySubscriber<MutableCollection<String>>) {
        liteEco.server.scheduler.runTaskAsynchronously(liteEco, Runnable {
            val uuid: List<UUID> = Arrays.stream(Bukkit.getOfflinePlayers()).parallel().map(OfflinePlayer::getUniqueId).collect(Collectors.toList())

            val identifiers: MutableList<String> = uuid.parallelStream()
                .filter{ puuid -> liteEco.api.hasAccount(Bukkit.getOfflinePlayer(puuid))}
                .map { puuid -> TreasuryAccount(liteEco, puuid) }
                .map(PlayerAccount::getIdentifier)
                .collect(Collectors.toList())

            subscription.succeed(identifiers)
        })
    }

    override fun retrieveNonPlayerAccountIds(subscription: EconomySubscriber<MutableCollection<String>>) {
        subscription.succeed(Collections.emptyList())
    }

    override fun getPrimaryCurrency(): Currency {
        return currency
    }

    override fun findCurrency(identifier: String): Optional<Currency> {
        return if (currency.identifier == currencyIdentifier) Optional.of(currency) else Optional.empty()
    }

    override fun getCurrencies(): MutableSet<Currency> {
        return Collections.singleton(currency)
    }

    override fun registerCurrency(currency: Currency, subscription: EconomySubscriber<Boolean>) {
        subscription.succeed(false)
    }
}