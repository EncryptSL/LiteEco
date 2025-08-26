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


class UpdateNotifier(
    private val liteEco: LiteEco,
    private val resourceId: String,
    private val currentVersion: String
) {
    companion object {
        private const val API_URL = "https://api.spiget.org/v2/resources/%s/versions/latest"
    }

    fun checkForUpdateAsync() {

        val client = HttpClient(Java) {
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

        liteEco.pluginScope.launch {
            try {
                val response: HttpResponse = client.get(API_URL.format(resourceId))

                if (response.status != HttpStatusCode.OK) {
                    logError("Update check failed with HTTP ${response.status.value}")
                    return@launch
                }
                val responseBody = response.bodyAsText()
                if (responseBody.isBlank()) {
                    logError("Update check returned empty response body.")
                    return@launch
                }

                val version: Version = client.get(API_URL.format(resourceId)).body()
                logUpdate(version)
            } catch (e: Exception) {
                logError("Exception during update check: ${e.localizedMessage}")
            }
        }
    }

    private fun logUpdate(version: Version) {
        val message = if (version.name.equals(currentVersion, ignoreCase = true)) {
            "<green>You are using the current version!"
        } else {
            "<green>New version available <dark_green>[${version.name}]</dark_green>, " +
                    "<green>you are using <dark_green>[$currentVersion]</dark_green>"
        }
        liteEco.componentLogger.info(ModernText.miniModernText(message))
    }

    private fun logError(message: String) {
        liteEco.componentLogger.error(message)
    }

    data class Version(val name: String, val id: String)
}