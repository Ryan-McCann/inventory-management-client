package app.ryanm.homeinventory.inventory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item (var id: Int = 0,
                 var description: String = "",
                 var minimum: Int = 0,
                 var maximum: Int = 0,
                 var quantity: Int = 0,
                 var barcodes:ArrayList<String> = ArrayList(),
                 var shelfQuantities: ArrayList<ShelfQuantity> = ArrayList()): Parcelable