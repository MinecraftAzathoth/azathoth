package com.azathoth.services.admin.api.routes

import com.azathoth.services.admin.api.model.LoginRequest
import com.azathoth.services.admin.api.model.RefreshRequest
import com.azathoth.services.admin.auth.AuthService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * 认证路由
 */
fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val result = authService.login(request.username, request.password)
            if (result.success) {
                call.respond(result)
            } else {
                call.respond(HttpStatusCode.Unauthorized, result)
            }
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val result = authService.refresh(request.refreshToken)
            if (result.success) {
                call.respond(result)
            } else {
                call.respond(HttpStatusCode.Unauthorized, result)
            }
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.subject ?: return@post call.respond(HttpStatusCode.Unauthorized)
                authService.logout(userId)
                call.respond(HttpStatusCode.OK)
            }

            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.subject ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = authService.getUserById(userId)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
