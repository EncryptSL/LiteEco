package com.github.encryptsl.lite.eco.api

import com.google.gson.Gson
import com.github.encryptsl.lite.eco.LiteEco
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class UpdateNotifier(private val liteEco: LiteEco, private val id: String, private val pluginVersion: String) {

    private val okHttpClient = OkHttpClient()
    private val requestClient = Request.Builder()

    fun makeUpdateCheck() {
        try {
            val request: Request = requestClient.url(String.format("https://api.spiget.org/v2/resources/%s/versions/latest", id)).build()
            val call: Call = okHttpClient.newCall(request)
            call.execute().use { r ->
                if (!r.isSuccessful) { r.close() }
                liteEco.logger.info(logUpdate(r))
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    private fun logUpdate(response: Response): String {
        if (response.code != 200)
            throw Exception("Error during check update.. response code ${response.code}")

        val latestVersion = Gson().fromJson(response.body?.string(), Version::class.java).name

        return when(latestVersion.equals(pluginVersion, true)) {
            true -> "You are using current version !"
            false -> {
                "Please download update of plugin LiteEco your version: $pluginVersion > Updated version: $latestVersion"
            }
        }
    }
    data class Version(val name: String)
}