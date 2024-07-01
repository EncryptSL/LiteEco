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
    enum class LangKey { CS_CZ, EN_US, ES_ES, JA_JP, DE_DE, PL_PL }

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
        val currentLocale: String = Optional.ofNullable(liteEco.config.getString("plugin.translation")).orElse(LangKey.EN_US.name)
        val fileName = "${getRequiredLocaleOrFallback(langKey, currentLocale)}.yml"
        val file = File("${liteEco.dataFolder}/locale/", fileName)
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                liteEco.saveResource("locale/$fileName", false)
            }
            val existingVersion = YamlConfiguration.loadConfiguration(file).getString("version")
            copyOutDateLocale(file, fileName, existingVersion)
            reloadLangFile(langKey, fileName)

            langYML = YamlConfiguration.loadConfiguration(file)
        } catch (e: Exception) {
            liteEco.logger.warning("Unsupported language, lang file for $langKey doesn't exist [!]")
            liteEco.logger.severe(e.message ?: e.localizedMessage)
            e.fillInStackTrace()
        }
    }

    private fun reloadLangFile(langKey: LangKey, fileName: String) {
        liteEco.config.set("plugin.translation", langKey.name)
        liteEco.saveConfig()
        liteEco.reloadConfig()
        liteEco.logger.info("Loaded translation $fileName [!]")
    }

    private fun copyOutDateLocale(file: File, fileName: String, version: String?) {
        if (version.isNullOrEmpty() || version != langVersion) {
            val backupFile = File(liteEco.dataFolder, "locale/old_$fileName")
            file.copyTo(backupFile, true)
            liteEco.saveResource("locale/$fileName", true)
        }
    }

    private fun getRequiredLocaleOrFallback(langKey: LangKey, currentLocale: String): String {
        return LangKey.entries.stream().map(LangKey::name).filter { el -> el.equals(langKey.name, true)}.findFirst().orElse(currentLocale).lowercase()
    }

    fun loadCurrentTranslation() {
        val optionalLocale: String = Optional.ofNullable(liteEco.config.getString("plugin.translation")).orElse(LangKey.EN_US.name)
        setLocale(LangKey.valueOf(optionalLocale))
    }
}