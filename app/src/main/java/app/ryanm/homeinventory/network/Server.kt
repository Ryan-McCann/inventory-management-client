package app.ryanm.homeinventory.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*

class Server {
    var url = ""

    suspend fun connect(connection:String): Boolean {
        val client = HttpClient(CIO)
        var connected = false

        try {
            val response: HttpResponse = client.submitForm(url = "$connection/validate-server.php")

            if(response.bodyAsText() == "home-inventory") {
                connected = true
                url = connection
            }
        } catch (e: Exception) {
            Log.e("Error:", e.toString())
        }

        return connected
    }

    suspend fun connected(): Boolean {
        return connect(url)
    }

    fun requestUrl(): String {
        return "$url/request.php"
    }

    fun registerUrl(): String {
        return "$url/register.php"
    }

    fun loginUrl(): String {
        return "$url/login.php"
    }

    fun signoutUrl(): String {
        return "$url/signout.php"
    }
}