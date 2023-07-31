package encryptsl.cekuj.net.config

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.LangKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.*

class TranslationConfig(private val liteEco: LiteEco) {

    private var langConfiguration: FileConfiguration? = null

    fun getMessage(value: String): String {
        return Optional.ofNullable(langConfiguration?.getString(value))
            .orElse(langConfiguration?.getString("messages.admin.translation_missing")?.replace("<key>", value))
    }

    fun getList(value: String): MutableList<*>? {
        return langConfiguration?.getList(value)
    }

    fun setTranslationFile(langKey: LangKey) {
        val file = File("${liteEco.dataFolder}/locale/", "${langKey.name.lowercase()}.yml")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            liteEco.saveResource("locale/${langKey.name.lowercase()}.yml", false)
        }
        try {
            file.createNewFile()
            liteEco.config.set("plugin.translation", langKey.name.lowercase())
            liteEco.saveConfig()
            liteEco.reloadConfig()
        } catch (e: IOException) {
            liteEco.getLogger().info("Unsupported language, lang file not exist !")
        }
        langConfiguration = YamlConfiguration.loadConfiguration(file)
    }

    fun reloadTranslationConfig() {
        loadTranslation()
    }

    fun loadTranslation() {
        val currentTranslation: String = liteEco.config.getString("plugin.translation")!!
        LangKey.values().find { it.name.lowercase() == currentTranslation }.apply {
            this?.let { setTranslationFile(it) }
        }
    }

}