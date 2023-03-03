package app.ryanm.homeinventory.inventory

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONTokener

class Inventory (var connectionId: String = "") {

    fun getItemByBarcode (barcode: String): Item {
        val json: JSONObject
        var item: Item = Item()

        val client = HttpClient(CIO)
        runBlocking {
            val response: HttpResponse = client.submitForm(
                url = "https://inventory.ryanm.app/",
                formParameters = Parameters.build {
                    append("token", connectionId)
                    append("requestType", "item")
                    append("barcode", barcode)
                }
            )
            val responseBody: String = response.bodyAsText()
            Log.i("Response: ", responseBody)
            // @TODO: Uncomment this once server side code generates data as JSON
            //json = JSONTokener(response.toString()).nextValue() as JSONObject
        }

        return item
    }
}