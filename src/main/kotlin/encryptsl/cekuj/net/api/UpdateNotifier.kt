package encryptsl.cekuj.net.api

import com.google.gson.Gson
import java.net.MalformedURLException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class UpdateNotifier(val id: String, val pluginVersion: String) {

    private val client = HttpClient.newHttpClient()
    private val httpRequest = HttpRequest.newBuilder()

    private fun makeCheckRequest(): String {
        httpRequest.uri(URI("https://api.spiget.org/v2/resources/$id/versions/latest"))
        val response = client.send(httpRequest.build(), HttpResponse.BodyHandlers.ofString())

        return response.body() ?: throw MalformedURLException("Request error > ${response.statusCode()}")
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