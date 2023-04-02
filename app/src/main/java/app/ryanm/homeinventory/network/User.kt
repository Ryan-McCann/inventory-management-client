package app.ryanm.homeinventory.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class User (var username:String = "", var token:String = "", var loggedIn:Boolean = false) {
    suspend fun register(username: String, password: String, server: Server): String {
        var result = "success"

        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitForm(
            url = server.registerUrl(),
            formParameters = Parameters.build {
                append("email", username)
                append("password", password)
                append("result", "text")
            }
        )

        result = response.bodyAsText().trimEnd()

        return result
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

       if(response.bodyAsText() == "user-disabled" || response.bodyAsText() == "invalid-password" || response.bodyAsText() == "invalid-user") {
           result = response.bodyAsText().trimEnd()
           loggedIn = false
       } else {
           token = response.bodyAsText().trimEnd()
           this@User.username = username
           loggedIn = true
       }

        return result
    }

    suspend fun login(server: Server): String {
        var result = "success"

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

        result = response.bodyAsText().trimEnd()

        return result
    }

    suspend fun loggedIn(server: Server): Boolean {
        return loggedIn
    }

    suspend fun signout(server: Server) {
        val client = HttpClient(CIO)
        client.submitForm (
            url = server.signoutUrl(),
            formParameters = Parameters.build {
                append("token", token)
            }
        )
    }
}