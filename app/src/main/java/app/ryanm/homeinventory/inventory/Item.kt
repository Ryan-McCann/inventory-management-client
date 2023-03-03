package app.ryanm.homeinventory.inventory

data class Item (val id: Int = 0, val description: String = "", val minimum: Int = 0, val maximum: Int = 0, val quantity: Int = 0, val barcodes: Array<String>)