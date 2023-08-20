package encryptsl.cekuj.net.utils

import org.bukkit.plugin.Plugin
import java.util.logging.Level

class DebugLogger(val plugin: Plugin) {
    fun debug(level: Level, message: String) {
        if (!plugin.config.getBoolean("plugin.debug")) return
        plugin.logger.log(level, message)
    }
}