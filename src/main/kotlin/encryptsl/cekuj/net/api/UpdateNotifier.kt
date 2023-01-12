package encryptsl.cekuj.net.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.MalformedURLException
import java.net.URL

class UpdateNotifier(private val id: String, private val pluginVersion: String) {

    private val okHttpClient = OkHttpClient()
    private val requestClient = Request.Builder()

    private fun makeCheckRequest(): String {
        val request = requestClient.url(URL(String.format("https://api.spiget.org/v2/resources/%s/versions/latest", id))).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                response.close()
                throw MalformedURLException("Request error > ${response.code}")
            }

            return response.body!!.string()
        }
    }

     private fun getVersion(): String {
        val data = makeCheckRequest()
        val gson = Gson()
        return gson.fromJson(data, Version::class.java).name
    }

    fun checkPluginVersion(): String {
        return when(getVersion()) {
            pluginVersion -> "You are using current version !"
            else -> {
                "Please download update of plugin LiteEco your version: $pluginVersion > updated version: ${getVersion()}"
            }
        }
    }
}

data class Version(
    val name: String
)