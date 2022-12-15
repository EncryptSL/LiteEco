package encryptsl.cekuj.net.api.events

import encryptsl.cekuj.net.api.enums.TransactionType
import org.bukkit.command.CommandSender
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ConsoleEconomyGlobalTransactionEvent(val commandSender: CommandSender, val transactionType: TransactionType, val money: Double) : Event(), Cancellable  {
    private var isCancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

    companion object {
        private val HANDLERS = HandlerList()
        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}