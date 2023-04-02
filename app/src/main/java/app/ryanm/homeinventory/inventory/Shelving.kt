package app.ryanm.homeinventory.inventory

import android.util.Log
import androidx.core.text.isDigitsOnly
import app.ryanm.homeinventory.network.Server
import app.ryanm.homeinventory.network.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class Shelving {
    suspend fun getShelfByLabel(label: String, server: Server, user: User) : Shelf {
        val shelf = Shelf()
        val json: JSONObject

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

    suspend fun getShelfById(id: Int, server: Server, user: User) : Shelf {
        val shelf = Shelf()
        val json: JSONObject

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelf")
                    append("shelfId", id.toString())
                }
            )
            json = JSONTokener(response.bodyAsText()).nextValue() as JSONObject
            shelf.id = id
            shelf.barcode = json.getString("barcode")
            shelf.label = json.getString("label")
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

    suspend fun getShelves(server: Server, user: User): ArrayList<Shelf> {
        val shelves = ArrayList<Shelf>()
        val json: JSONArray

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm (
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelves")
                }
            )

            json = JSONTokener(response.bodyAsText()).nextValue() as JSONArray
            for(i in 0 until json.length()) {
                val shelf = Shelf()
                val jsonObject: JSONObject = json.getJSONObject(i)

                shelf.id = jsonObject.getInt("id")
                shelf.label = jsonObject.getString("label")
                shelf.barcode = jsonObject.getString("barcode")

                shelves.add(shelf)
            }
        }

        return shelves
    }

    suspend fun getItemsByShelf(shelf: Shelf, server: Server, user: User) : ArrayList<Item> {
        val items = ArrayList<Item>()
        val json: JSONArray

        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelfItems")
                    append("shelfId", shelf.id.toString())
                }
            )

            json = JSONTokener(response.bodyAsText()).nextValue() as JSONArray

            for( i in 0 until json.length()) {
                val jsonObject = json.getJSONObject(i)
                val item = Item()
                item.id = jsonObject.getInt("id")
                item.description = jsonObject.getString("description")
                item.minimum = jsonObject.getInt("minimum")
                item.maximum = jsonObject.getInt("maximum")
                item.quantity = jsonObject.getInt("quantity")

                val barcodesJSON = jsonObject.getJSONArray("barcodes")

                for( j in 0 until barcodesJSON.length()) {
                    item.barcodes.add(barcodesJSON.getString(j))
                }

                val shelvesJSON = jsonObject.getJSONArray("shelves")

                for( k in 0 until shelvesJSON.length()) {
                    item.shelfIds.add(shelvesJSON.getInt(k))
                }

                items.add(item)
            }
        }

        return items
    }

    suspend fun getShelfItemQuantity(shelf: Shelf, item: Item, server: Server, user: User): Int {
        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            val result: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "shelfItemQuantity")
                    append("itemId", item.id.toString())
                    append("shelfId", shelf.id.toString())
                }
            )
            return if(result.bodyAsText().isDigitsOnly())
                result.bodyAsText().toInt()
            else
                0
        }

        return 0
    }

    suspend fun createShelf(label: String, server: Server, user: User) {
        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "createShelf")
                    append("label", label)
                }
            )
        }
    }

    suspend fun deleteShelf(shelf: Shelf, server: Server, user: User) {
        if(user.loggedIn(server)) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "deleteShelf")
                    append("shelfId", shelf.id.toString())
                }
            )
        }
    }

    suspend fun addItemToShelfByBarcode(barcode: String, quantity: Int, shelf: Shelf, server: Server, user: User) {
        if(user.loggedIn(server)) {
            val inventory = Inventory()
            val item = inventory.getItemByBarcode(barcode, server, user)

            val client = HttpClient(CIO)
            val response:HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "addItem")
                    append("itemId", item.id.toString())
                    append("shelfId", shelf.id.toString())
                    append("quantity", quantity.toString())
                }
            )
        }
    }

    suspend fun removeItemFromShelfByBarcode(barcode: String, quantity: Int, shelf: Shelf, server: Server, user: User) {
        if(user.loggedIn(server)) {
            val inventory = Inventory()
            val item = inventory.getItemByBarcode(barcode, server, user)

            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "removeItem")
                    append("itemId", item.id.toString())
                    append("shelfId", shelf.id.toString())
                    append("quantity", quantity.toString())
                }
            )
        }
    }
}