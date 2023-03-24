package app.ryanm.homeinventory.network

interface Network {
    fun getServer(): Server
    fun getUser(): User

    fun login(email: String, password: String, url: String)

    fun login(token: String, url: String)

    fun register(email: String, password: String, url: String)
}