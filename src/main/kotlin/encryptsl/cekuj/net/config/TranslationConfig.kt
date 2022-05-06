package encryptsl.cekuj.net.config

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TranslationKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.*

class TranslationConfig(private val liteEco: LiteEco) {

    private var langConfiguration: FileConfiguration? = null

    fun getMessage(value: String): String {
        return Optional.ofNullable(langConfiguration?.getString(value))
            .orElse(langConfiguration?.getString("messages.translation_missing")?.replace("<key>", value))
    }

    fun getList(value: String): MutableList<*>? {
        return langConfiguration?.getList(value)
    }

    fun setTranslationFile(translationKey: TranslationKey) {
        val file = File("${liteEco.dataFolder}/locale/", "translation-${translationKey.name}.yml")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            liteEco.saveResource("locale/translation-${translationKey.name}.yml", false)
        }
        try {
            file.createNewFile()
            liteEco.config.set("plugin.translation", translationKey.name)
            liteEco.saveConfig()
            liteEco.reloadConfig()
        } catch (e: IOException) {
            liteEco.logger.info("Unsupported language, lang file not exist !")
        }
        langConfiguration = YamlConfiguration.loadConfiguration(file)
    }

    fun reloadTranslationConfig() {
        loadTranslation()
    }

    fun loadTranslation() {
        val currentTranslation: String = liteEco.config.getString("plugin.translation")!!
        when(TranslationKey.valueOf(currentTranslation)) {
            TranslationKey.CS_CZ -> setTranslationFile(TranslationKey.CS_CZ)
            TranslationKey.EN_US -> setTranslationFile(TranslationKey.EN_US)
        }
    }

}