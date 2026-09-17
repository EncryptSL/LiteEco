package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


enum class UpdateProvider {
    HANGAR,
    SPIGET
}

class UpdateNotifier(
    private val liteEco: LiteEco,
    private val currentVersion: String,
    private val providers: List<UpdateProvider> = listOf(UpdateProvider.HANGAR, UpdateProvider.SPIGET),
    private val hangarSlug: String = "encryptsl/liteeco",
    private val resourceId: String = "101716"
) {
    @OptIn(ExperimentalAtomicApi::class)
    private val hasRun = AtomicBoolean(false)

    companion object {
        private const val HANGAR_API_URL = "https://hangar.papermc.io/api/v1/projects/%s/latest?channel=release"
        private const val SPIGET_API_URL = "https://api.spiget.org/v2/resources/%s/versions/latest"
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun checkForUpdateAsync() {
        if (!hasRun.compareAndSet(false, true)) return

        liteEco.pluginScope.launch {
            createHttpClient().use { client ->
                for (provider in providers) {
                    val latestVersion = try {
                        when (provider) {
                            UpdateProvider.HANGAR -> fetchHangarVersion(client)
                            UpdateProvider.SPIGET -> fetchSpigetVersion(client)
                        }
                    } catch (e: Exception) {
                        logError("Failed to fetch update from $provider: ${e.localizedMessage}")
                        null
                    }

                    if (latestVersion != null) {
                        logUpdate(latestVersion, provider)
                        return@launch
                    }
                }

                logError("All update providers failed to retrieve the latest version.")
            }
        }
    }

    private suspend fun fetchHangarVersion(client: HttpClient): String? {
        val response: HttpResponse = client.get(HANGAR_API_URL.format(hangarSlug)) {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) LiteEco-UpdateNotifier/1.0")
            accept(ContentType.Text.Plain)
        }

        if (response.status != HttpStatusCode.OK) {
            logError("Hangar returned status code ${response.status.value} for URL: ${response.request.url}")
            return null
        }

        val bodyText = response.bodyAsText().trim().removeSurrounding("\"").removePrefix("v")
        return bodyText.ifBlank { null }
    }

    private suspend fun fetchSpigetVersion(client: HttpClient): String? {
        val response: HttpResponse = client.get(SPIGET_API_URL.format(resourceId)) {
            header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) LiteEco-UpdateNotifier/1.0")
            accept(ContentType.Application.Json)
        }

        if (response.status != HttpStatusCode.OK) {
            logError("Spiget returned status code ${response.status.value} for URL: ${response.request.url}")
            return null
        }

        return response.body<SpigetVersion>().name
    }

    private fun createHttpClient(): HttpClient {
        return HttpClient(Java) {
            install(HttpRedirect) {
                checkHttpMethod = false
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 5000
            }

            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
        }
    }

    private fun logUpdate(latestVersionStr: String, provider: UpdateProvider) {
        val current = VersionParser.parse(currentVersion)
        val latest = VersionParser.parse(latestVersionStr)

        if (current == null || latest == null) {
            logError("Could not parse versions: current='$currentVersion', latest='$latestVersionStr'")
            return
        }

        val message = when {
            current > latest -> {
                "<gold>You are running an unreleased development build <yellow>[${current.rawVersion}]</yellow>! " +
                        "Latest published release on $provider is <dark_green>[${latest.toCleanString()}]</dark_green>."
            }
            current < latest -> {
                "<green>New version available <dark_green>[${latest.toCleanString()}]</dark_green>, " +
                        "you are using <dark_green>[${current.toCleanString()}]</dark_green> <gray>($provider)</gray>"
            }
            else -> {
                "<green>You are using the current version! <gray>($provider)</gray>"
            }
        }

        liteEco.componentLogger.info(ModernText.miniModernText(message))
    }

    private fun logError(message: String) {
        liteEco.componentLogger.error(message)
    }

    private data class SpigetVersion(val name: String, val id: String)

    data class Version(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val isSnapshot: Boolean,
        val rawVersion: String
    ) : Comparable<Version> {

        override fun compareTo(other: Version): Int {
            if (this.major != other.major) return this.major.compareTo(other.major)
            if (this.minor != other.minor) return this.minor.compareTo(other.minor)
            return this.patch.compareTo(other.patch)
        }

        fun toCleanString(): String = "$major.$minor.$patch"
    }

    object VersionParser {

        private val VERSION_REGEX = Regex("""^v?(\d+)\.(\d+)(?:\.(\d+))?(?:-(SNAPSHOT))?$""", RegexOption.IGNORE_CASE)

        fun parse(versionString: String): Version? {
            val trimmed = versionString.trim()
            val matchResult = VERSION_REGEX.find(trimmed) ?: return null
            val (majorStr, minorStr, patchStr, snapshotSuffix) = matchResult.destructured

            val major = majorStr.toIntOrNull() ?: return null
            val minor = minorStr.toIntOrNull() ?: return null
            val patch = patchStr.ifEmpty { "0" }.toIntOrNull() ?: 0

            return Version(
                major = major,
                minor = minor,
                patch = patch,
                isSnapshot = snapshotSuffix.equals("SNAPSHOT", ignoreCase = true),
                rawVersion = trimmed
            )
        }
    }
}