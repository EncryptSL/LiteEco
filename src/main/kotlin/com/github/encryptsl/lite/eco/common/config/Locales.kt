package com.github.encryptsl.lite.eco.common.config

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.tchristofferson.configupdater.ConfigUpdater
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Locales(private val liteEco: LiteEco) {
    enum class LangKey { CS_CZ, EN_US, ES_ES, JA_JP, DE_DE, PL_PL, PT_BR, TR_TR, ZH_CN }

    private lateinit var langYml: FileConfiguration
    private val config get() = liteEco.config
    private val logger get() = liteEco.logger

    private val localeDir = File(liteEco.dataFolder, "locale")

    val prefix: String
        get() = config.getString("plugin.prefix").orEmpty()

    fun translation(translationKey: String): Component {
        return ModernText.miniModernText(getMessage(translationKey))
    }

    fun translation(translationKey: String, tagResolver: TagResolver): Component {
        return ModernText.miniModernText(getMessage(translationKey), tagResolver)
    }

    fun plainTextTranslation(component: Component): String {

        return PlainTextComponentSerializer.plainText().serialize(component)
    }

    fun getMessage(key: String): String {
        val raw = langYml.getString(key)
            ?: langYml.getString("messages.admin.translation_missing")?.replace("<key>", key)
        return raw?.replace("<prefix>", prefix)
            ?: "Translation missing: $key"
    }

    fun getList(key: String): List<String> {
        return langYml.getList(key)
            ?.mapNotNull { it?.toString()?.replace("<prefix>", prefix) }
            ?: emptyList()
    }

    fun setLocale(langKey: LangKey) {
        val selectedLocale = resolveLocale(langKey)
        val fileName = "$selectedLocale.yml"
        val file = File(localeDir, fileName)

        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                liteEco.saveResource("locale/$fileName", false)
            }

            ConfigUpdater.update(liteEco, "locale/$fileName", file)
            langYml = YamlConfiguration.loadConfiguration(file)

            config.set("plugin.translation", langKey.name)
            liteEco.saveConfig()

            logger.info("✅ Translation loaded: $fileName")

        } catch (ex: Exception) {
            logger.warning("⚠️ Failed to load translation for ${langKey.name}")
            logger.severe(ex.message ?: ex.toString())
        }
    }

    fun reloadCurrentLocale() {
        val current = config.getString("plugin.translation").orEmpty()
        val langKey = runCatching { LangKey.valueOf(current) }.getOrDefault(LangKey.EN_US)
        setLocale(langKey)
    }

    fun load() {
        val selected = config.getString("plugin.translation").orEmpty()
        val langKey = runCatching { LangKey.valueOf(selected) }.getOrDefault(LangKey.EN_US)
        setLocale(langKey)
    }

    private fun resolveLocale(langKey: LangKey): String {
        return LangKey.entries
            .firstOrNull { it.name.equals(langKey.name, ignoreCase = true) }
            ?.name?.lowercase()
            ?: "en_us"
    }
}