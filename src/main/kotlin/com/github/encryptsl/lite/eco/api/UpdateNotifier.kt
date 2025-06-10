package com.github.encryptsl.lite.eco.api

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request


class UpdateNotifier(
    private val liteEco: LiteEco,
    private val resourceId: String,
    private val currentVersion: String
) {
    companion object {
        private const val API_URL = "https://api.spiget.org/v2/resources/%s/versions/latest"
    }

    private val httpClient = OkHttpClient()
    private val gson = Gson()

    fun checkForUpdateAsync() {
        liteEco.pluginScope.launch {
            try {
                val request = Request.Builder()
                    .url(API_URL.format(resourceId))
                    .build()

                val response = httpClient.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        logError("Update check failed with HTTP ${it.code}")
                        return@use
                    }

                    val responseBody = it.body?.string()
                    if (responseBody == null) {
                        logError("Update check returned empty response body.")
                        return@use
                    }
                    val version = gson.fromJson(responseBody, Version::class.java)
                    logUpdate(version)
                }
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