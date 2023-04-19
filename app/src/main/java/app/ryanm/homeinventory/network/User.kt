package app.ryanm.homeinventory.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class User (var username:String = "", var token:String = "", var loggedIn:Boolean = false) {
    private var loginListener: (()->Unit)? = null
    private var signoutListener: (()->Unit)? = null

    fun setOnLoginListener(lambda: (()->Unit)) {
        loginListener = lambda
    }

    fun setOnSignoutListener(lambda: (()->Unit)) {
        signoutListener = lambda
    }

    suspend fun register(username: String, password: String, server: Server): String {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitForm(
            url = server.registerUrl(),
            formParameters = Parameters.build {
                append("email", username)
                append("password", password)
                append("result", "text")
            }
        )

        return response.bodyAsText().trimEnd()
    }

    suspend fun login(username: String, password: String, server: Server): String {
        var result = "success"

        // Send login request to server
        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitForm(
            url = server.loginUrl(),
            formParameters = Parameters.build {
                append("email", username)
                append("password", password)
                append("result", "text")
            }
        )

       if(response.bodyAsText().trimEnd() == "user-disabled"
           || response.bodyAsText().trimEnd() == "invalid-password"
           || response.bodyAsText().trimEnd() == "invalid-user") {
           result = response.bodyAsText().trimEnd()
           loggedIn = false
       } else {
           token = response.bodyAsText().trimEnd()
           this@User.username = username
           loggedIn = true
           loginListener?.invoke()
       }

        return result
    }

    suspend fun login(server: Server): String {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitForm(
            url = server.loginUrl(),
            formParameters = Parameters.build {
                append("email", username)
                append("token", token)
                append("result", "text")
            }
        )

        loggedIn = response.bodyAsText().trimEnd() == username

        if(loggedIn)
            loginListener?.invoke()

        return response.bodyAsText().trimEnd()
    }

    suspend fun signout(server: Server) {
        val client = HttpClient(CIO)
        client.submitForm (
            url = server.signoutUrl(),
            formParameters = Parameters.build {
                append("token", token)
            }
        )

        username = ""
        token = ""
        server.url = ""
        loggedIn = false

        signoutListener?.invoke()
    }
}