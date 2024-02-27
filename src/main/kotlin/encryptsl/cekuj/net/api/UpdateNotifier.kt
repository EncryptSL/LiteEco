package encryptsl.cekuj.net.api

import com.google.gson.Gson
import encryptsl.cekuj.net.LiteEco
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
            throw Exception("Excepted http code is 200 but returned is ${response.code}")

        return when(val latestVersion = Gson().fromJson(response.body?.string(), Version::class.java).name) {
            latestVersion -> "You are using current version !"
            else -> {
                "Please download update of plugin LiteEco your version: $pluginVersion > Updated version: $latestVersion"
            }
        }
    }

}

data class Version(
    val name: String
)