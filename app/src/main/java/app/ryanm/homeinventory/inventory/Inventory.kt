package app.ryanm.homeinventory.inventory

import app.ryanm.homeinventory.network.Server
import app.ryanm.homeinventory.network.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import org.json.JSONObject
import org.json.JSONTokener

class Inventory {
    suspend fun getItemByBarcode (barcode: String, server: Server, user: User): Item {
        val json: JSONObject
        val item = Item()

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "item")
                    append("barcode", barcode)
                }
            )
            val responseBody: String = response.bodyAsText()

            json = JSONTokener(responseBody).nextValue() as JSONObject
            item.id = json.getInt("id")
            item.description = json.getString("description")
            item.minimum = json.getInt("minimum")
            item.maximum = json.getInt("maximum")
            item.quantity = json.getInt("quantity")
            for(i in 0 until json.getJSONArray("barcodes").length())
            {
                item.barcodes[i] = json.getJSONArray("barcodes")[i].toString()
            }
        }

        return item
    }
}