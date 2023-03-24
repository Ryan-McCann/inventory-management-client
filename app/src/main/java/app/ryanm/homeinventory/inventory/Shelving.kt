package app.ryanm.homeinventory.inventory

import app.ryanm.homeinventory.network.Server
import app.ryanm.homeinventory.network.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONTokener

class Shelving {
    suspend fun getShelfByLabel(label: String, server: Server, user: User) : Shelf {
        val shelf = Shelf()
        var json: JSONObject

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)

            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelf")
                    append("label", label)
                }
            )
            json = JSONTokener(response.bodyAsText()).nextValue() as JSONObject
            shelf.id = json.getInt("id")
            shelf.label = json.getString("label")
            shelf.barcode = json.getString("barcode")
        }

        return shelf
    }

    suspend fun getShelfByBarcode(barcode: String, server: Server, user: User) : Shelf {
        val shelf = Shelf()
        val json: JSONObject

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelf")
                    append("barcode", barcode)
                }
            )
            json = JSONTokener(response.bodyAsText()).nextValue() as JSONObject
            shelf.id = json.getInt("id")
            shelf.label = json.getString("label")
            shelf.barcode = json.getString("barcode")
        }

        return shelf
    }
}