package app.ryanm.homeinventory.ui

import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.inventory.Shelf

interface IFragComm {
    fun setItemFragment(item: Item)

    fun setShelfFragment(shelf: Shelf)
}