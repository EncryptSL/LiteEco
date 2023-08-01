package encryptsl.cekuj.net.config

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.LangKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.*

class Locales(private val liteEco: LiteEco) {

    private var langYML: FileConfiguration? = null

    fun getMessage(value: String): String {
        val key = langYML?.getString(value) ?:
        langYML?.getString("messages.admin.translation_missing")?.replace("<key>", value)
        val prefix = liteEco.config.getString("plugin.prefix")

        return key?.replace("<prefix>", prefix ?: "") ?: "Translation missing error: $value"
    }

    fun getList(value: String): MutableList<*>? {
        val list = langYML?.getList(value)?.toMutableList()
        val prefix = liteEco.config.getString("plugin.prefix")
        list?.replaceAll { it?.toString()?.replace("<prefix>", prefix ?: "") }

        return list
    }

    fun setTranslationFile(langKey: LangKey) {
        val fileName = "${langKey.name.lowercase()}.yml"
        val file = File("${liteEco.dataFolder}/locale/", fileName)

        if (!file.exists() && file.parentFile.mkdirs()) {
            liteEco.saveResource("locale/$fileName", false)
        }

        try {
            file.createNewFile()
            liteEco.config["plugin.translation"] = langKey.name
            liteEco.saveConfig()
            liteEco.reloadConfig()
            liteEco.logger.info("Loaded translation $fileName [!]")
        } catch (e: IOException) {
            liteEco.logger.warning("Unsupported language, lang file for $langKey doesn't exist [!]")
            return
        }
        langYML = YamlConfiguration.loadConfiguration(file)
    }

    fun reloadTranslation() {
        val currentLocale: String = liteEco.config.getString("plugin.translation") ?: return
        LangKey.values().find { it.name.equals(currentLocale, ignoreCase = true) }?.let {
            setTranslationFile(it)
        }
    }
}