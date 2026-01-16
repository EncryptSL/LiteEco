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

class Locales(
    private val liteEco: LiteEco
) {

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

    fun translationList(translationKey: String): List<Component> {
        return getList(translationKey).map { ModernText.miniModernText(it) }
    }

    fun translationList(translationKey: String, tagResolver: TagResolver): List<Component> {
        return getList(translationKey).map { ModernText.miniModernText(it, tagResolver) }
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

    private fun getList(key: String): List<String> {
        return langYml.getList(key)
            ?.mapNotNull { it?.toString()?.replace("<prefix>", prefix) }
            ?: emptyList()
    }

    fun setLocale(localeCode: String): Boolean {
        val normalizedLocale = localeCode.lowercase()
        val fileName = "$normalizedLocale.yml"
        val file = File(localeDir, fileName)

        return try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                val resourcePath = "locale/$fileName"

                if (liteEco.getResource(resourcePath) != null) {
                    liteEco.saveResource(resourcePath, false)
                } else {
                    logger.warn("⚠️ Translation resource '$resourcePath' not found. Falling back to en_us.")
                    return if (normalizedLocale != "en_us") setLocale("en_us") else false
                }
            }

            ConfigUpdater.update(liteEco, "locale/$fileName", file)
            langYml = YamlConfiguration.loadConfiguration(file)

            config.set("plugin.translation", normalizedLocale)
            liteEco.saveConfig()

            logger.info("✅ Translation loaded: $fileName")
            true
        } catch (ex: Exception) {
            logger.warn("⚠️ Failed to load translation for $localeCode")
            logger.warn(ex.message ?: ex.toString())
            false
        }
    }

    fun reloadCurrentLocale() {
        val current = config.getString("plugin.translation").orEmpty()
        setLocale(current.ifBlank { "en_us" })
    }

    fun load() {
        val selected = config.getString("plugin.translation").orEmpty()
        setLocale(selected.ifBlank { "en_us" })
    }

    fun availableLocales(): List<String> {
        val fromJar = runCatching { availableLocalesFromJar() }.getOrElse { emptyList() }
        val fromDisk = localeDir.listFiles()
            ?.filter { it.isFile && it.extension == "yml" }
            ?.map { it.nameWithoutExtension.lowercase() }
            ?: emptyList()

        return (fromJar + fromDisk).distinct()
    }

    fun isLocaleAvailable(locale: String): Boolean {
        val normalized = locale.lowercase()
        return availableLocales().contains(normalized)
                || liteEco.getResource("locale/$normalized.yml") != null
    }

    private fun availableLocalesFromJar(): List<String> {
        val locales = mutableListOf<String>()

        val jarUrl = this::class.java.protectionDomain.codeSource.location
        val jarFile = java.util.jar.JarFile(File(jarUrl.toURI()))

        jarFile.use { jar ->
            for (entry in jar.entries()) {
                if (entry.name.startsWith("locale/") && entry.name.endsWith(".yml")) {
                    val localeName = entry.name.substringAfter("locale/").substringBeforeLast(".yml")
                    locales.add(localeName.lowercase())
                }
            }
        }

        return locales
    }
}