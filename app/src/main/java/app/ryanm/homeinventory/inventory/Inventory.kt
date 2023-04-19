package app.ryanm.homeinventory.inventory

import app.ryanm.homeinventory.network.Server
import app.ryanm.homeinventory.network.User
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class Inventory {
    suspend fun getItemById(id: Int, server: Server, user: User): Item {
        val json: JSONObject
        val item = Item()

        if(user.loggedIn) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "item")
                    append("itemId", id.toString())
                }
            )
            val responseBody: String = response.bodyAsText()

            json = JSONTokener(responseBody).nextValue() as JSONObject
            item.id = json.getInt("id")
            item.description = json.getString("description")
            item.minimum = json.getInt("minimum")
            item.maximum = json.getInt("maximum")
            item.quantity = json.getInt("quantity")
            for(i in 0 until json.getJSONArray("barcodes").length()) {
                item.barcodes.add(json.getJSONArray("barcodes")[i].toString())
            }

            for(i in 0 until json.getJSONArray("shelves").length()) {
                val shelfJSON:JSONObject = json.getJSONArray("shelves")[i] as JSONObject

                val shelfId = shelfJSON.getInt("id")
                val shelfLabel = shelfJSON.getString("label")
                val shelfBarcode = shelfJSON.getString("barcode")
                val itemQuantity = shelfJSON.getInt("item_quantity")

                item.shelfQuantities.add(ShelfQuantity(shelfId, shelfLabel, shelfBarcode, itemQuantity))
            }
        }

        return item
    }

    suspend fun getItemByBarcode (barcode: String, server: Server, user: User): Item {
        val json: JSONObject
        val item = Item()

        if(user.loggedIn) {
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
            for(i in 0 until json.getJSONArray("barcodes").length()) {
                item.barcodes.add(json.getJSONArray("barcodes")[i].toString())
            }

            for(i in 0 until json.getJSONArray("shelves").length()) {
                val shelfJSON:JSONObject = json.getJSONArray("shelves")[i] as JSONObject

                val shelfId = shelfJSON.getInt("id")
                val shelfLabel = shelfJSON.getString("label")
                val shelfBarcode = shelfJSON.getString("barcode")
                val itemQuantity = shelfJSON.getInt("item_quantity")

                item.shelfQuantities.add(ShelfQuantity(shelfId, shelfLabel, shelfBarcode, itemQuantity))
            }
        }

        return item
    }

    suspend fun getItems(server: Server, user: User): ArrayList<Item> {
        val items = ArrayList<Item>()

        if(user.loggedIn) {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "items")
                }
            )

            val json = JSONTokener(response.bodyAsText()).nextValue() as JSONArray

            for(i in 0 until json.length()) {
                val item = Item()
                val itemJSON:JSONObject = json.getJSONObject(i)
                item.id = itemJSON.getInt("id")
                item.description = itemJSON.getString("description")
                item.minimum = itemJSON.getInt("minimum")
                item.maximum = itemJSON.getInt("maximum")
                item.quantity = itemJSON.getInt("quantity")

                items.add(item)
            }
        }

        return items
    }

    suspend fun associateItem (item: Item, barcode: String, server: Server, user: User) {
        if(user.loggedIn) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build{
                    append("token", user.token)
                    append("type", "createAlias")
                    append("itemId", item.id.toString())
                    append("barcode", barcode)
                }
            )
        }
    }

    suspend fun removeAlias(barcode: String, server: Server, user: User) {
        if(user.loggedIn) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "deleteAlias")
                    append("barcode", barcode)
                }
            )
        }
    }

    suspend fun createItem (item: Item, server: Server, user: User) {
        if(user.loggedIn) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "createItem")
                    append("description", item.description)
                    append("minimum", item.minimum.toString())
                    append("maximum", item.maximum.toString())
                    append("barcode", item.barcodes[0])
                }
            )
        }
    }

    suspend fun updateItem (item: Item, server: Server, user: User) {
        if(user.loggedIn) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "updateItem")
                    append("itemId", item.id.toString())
                    append("description", item.description)
                    append("minimum", item.minimum.toString())
                    append("maximum", item.maximum.toString())
                }
            )
        }
    }

    suspend fun deleteItem(item: Item, server: Server, user: User) {
        if(user.loggedIn) {
            val client = HttpClient(CIO)
            client.submitForm(
                url = server.requestUrl(),
                formParameters = Parameters.build {
                    append("token", user.token)
                    append("type", "deleteItem")
                    append("itemId", item.id.toString())
                }
            )
        }
    }
}