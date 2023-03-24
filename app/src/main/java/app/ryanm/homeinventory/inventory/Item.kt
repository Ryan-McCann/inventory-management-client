package app.ryanm.homeinventory.inventory

data class Item (var id: Int = 0, var description: String = "", var minimum: Int = 0, var maximum: Int = 0, var quantity: Int = 0, var barcodes: Array<String> = emptyArray(), var shelfIds: Array<Int> = emptyArray()) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (id != other.id) return false
        if (description != other.description) return false
        if (minimum != other.minimum) return false
        if (maximum != other.maximum) return false
        if (quantity != other.quantity) return false
        if (!barcodes.contentEquals(other.barcodes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + description.hashCode()
        result = 31 * result + minimum
        result = 31 * result + maximum
        result = 31 * result + quantity
        result = 31 * result + barcodes.contentHashCode()
        return result
    }
}