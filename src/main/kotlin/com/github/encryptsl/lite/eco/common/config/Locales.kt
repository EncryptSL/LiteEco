package com.github.encryptsl.lite.eco.common.config

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.Optional

class Locales(private val liteEco: LiteEco, private val langVersion: String) {
    enum class LangKey { CS_CZ, EN_US, ES_ES, JA_JP, DE_DE, }

    private var langYML: FileConfiguration? = null

    fun translation(translationKey: String): Component {
        return ModernText.miniModernText(getMessage(translationKey))
    }

    fun translation(translationKey: String, tagResolver: TagResolver): Component {

        return ModernText.miniModernText(getMessage(translationKey), tagResolver)
    }

    fun getMessage(value: String): String {
        val key = Optional.ofNullable(langYML?.getString(value)).orElse(langYML?.getString("messages.admin.translation_missing")?.replace("<key>", value))
        val prefix = liteEco.config.getString("plugin.prefix", "").toString()
        return Optional.ofNullable(key?.replace("<prefix>", prefix)).orElse("Translation missing error: $value")
    }

    fun getList(value: String): MutableList<*>? {
        val list = langYML?.getList(value)?.toMutableList()
        val prefix = liteEco.config.getString("plugin.prefix", "").toString()
        list?.replaceAll { it?.toString()?.replace("<prefix>", prefix) }

        return list
    }

    fun setLocale(langKey: LangKey) {
        val currentLocale: String = Optional.ofNullable(liteEco.config.getString("plugin.translation")).orElse("CS")
        val fileName = "message_${getRequiredLocaleOrFallback(langKey, currentLocale)}.yml"
        val file = File("${liteEco.dataFolder}/locale/", fileName)
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                liteEco.saveResource("locale/$fileName", false)
            }
            val existingVersion = YamlConfiguration.loadConfiguration(file).getString("version")
            if (existingVersion.isNullOrEmpty() || existingVersion != langVersion) {
                val backupFile = File(liteEco.dataFolder, "locale/old_$fileName")
                file.copyTo(backupFile, true)
                liteEco.saveResource("locale/$fileName", true)
            }

            liteEco.config.set("plugin.translation", langKey.name)
            liteEco.saveConfig()
            liteEco.reloadConfig()
            liteEco.logger.info("Loaded translation $fileName [!]")

            langYML = YamlConfiguration.loadConfiguration(file)
        } catch (_: Exception) {
            liteEco.logger.warning("Unsupported language, lang file for $langKey doesn't exist [!]")
        }
    }

    private fun getRequiredLocaleOrFallback(langKey: LangKey, currentLocale: String): String {
        return LangKey.entries.stream().map<String>(LangKey::name).filter {el -> el.equals(langKey.name, true)}.findFirst().orElse(currentLocale)
    }

    fun loadCurrentTranslation() {
        val optionalLocale: String = Optional.ofNullable(liteEco.config.getString("plugin.translation")).orElse("CS")
        setLocale(LangKey.valueOf(optionalLocale))
    }
}