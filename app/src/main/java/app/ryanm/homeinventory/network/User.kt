package app.ryanm.homeinventory.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

class User (var username:String = "", var token:String = "") {
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
       } else {
           token = response.bodyAsText().trimEnd()
           this@User.username = username
       }

        return result
    }

    suspend fun loggedIn(server: Server): Boolean {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.submitForm(
            url = server.loginUrl(),
            formParameters = Parameters.build {
                append("token", token)
            }
        )

        return response.bodyAsText().trimEnd() == username
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