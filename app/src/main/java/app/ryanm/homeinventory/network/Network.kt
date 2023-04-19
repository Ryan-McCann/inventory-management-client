package app.ryanm.homeinventory.network

interface Network {
    fun getServer(): Server
    fun getUser(): User
}