package com.github.encryptsl.lite.eco.api

import com.google.gson.Gson
import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.objects.ModernText
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class UpdateNotifier(private val liteEco: LiteEco, private val id: String, private val pluginVersion: String) {

    private val okHttpClient = OkHttpClient()
    private val requestClient = Request.Builder()

    companion object {
        const val API_LINK = "https://api.spiget.org/v2/resources/%s/versions/latest"
    }

    fun makeUpdateCheck() {
        try {
            val request: Request = requestClient.url(String.format(API_LINK, id)).build()
            val call: Call = okHttpClient.newCall(request)
            call.execute().use { r ->
                if (!r.isSuccessful) { r.close() }
                liteEco.componentLogger.info(ModernText.miniModernText(logUpdate(r)))
            }
        } catch (e : Exception) {
            liteEco.componentLogger.error(e.message ?: e.localizedMessage)
        }
    }

    private fun logUpdate(response: Response): String {
        if (response.code != 200)
            return "<red>Error during check update.. response code, <yellow>${response.code}"

        val data = Gson().fromJson(response.body?.string(), Version::class.java)

        return when(data.name.equals(pluginVersion, true)) {
            true -> "<green>You are using current version !"
            false -> {
                "<green>New version is here <dark_green>[${data.name}]</dark_green> <green>you are using <dark_green>[$pluginVersion]"
            }
        }
    }
    data class Version(val name: String, val id: String)
}