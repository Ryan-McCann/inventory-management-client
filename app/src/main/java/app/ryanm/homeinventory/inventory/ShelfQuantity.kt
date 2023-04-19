package app.ryanm.homeinventory.inventory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShelfQuantity(val shelfId: Int = 0, val label: String = "", val barcode: String = "", val itemQuantity: Int = 0):
    Parcelable

