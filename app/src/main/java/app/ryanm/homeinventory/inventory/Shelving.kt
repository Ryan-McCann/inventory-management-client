package app.ryanm.homeinventory.inventory

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONTokener

class Shelving (connectionId: String = "") {
    var connectionId: String = connectionId

    fun getShelfByLabel(label: String) : Shelf {
        var shelf: Shelf = Shelf()



        return shelf
    }

    fun getShelfByBarcode(barcode: String) : Shelf {
        var shelf: Shelf = Shelf()
        val json: JSONObject

        val client = HttpClient(CIO)
        runBlocking {
            val response: HttpResponse = client.submitForm(
                url = "https://inventory.ryanm.app/",
                formParameters = Parameters.build {
                    append("token", connectionId)
                    append("requestType", "shelf")
                    append("barcode", barcode)
                }
            )
            Log.i("Response: ", response.bodyAsText())
            // @TODO: Uncomment this once server generates data as json
            //json = JSONTokener(response.toString()).nextValue() as JSONObject
        }

        return shelf
    }
}