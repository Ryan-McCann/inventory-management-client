package app.ryanm.homeinventory.inventory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shelf(var id:Int = 0,
                 var label:String = "",
                 var barcode: String = ""): Parcelable