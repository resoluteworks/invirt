package examples.authentication.handlers

import examples.authentication.service.AuthenticationService
import invirt.core.GET
import invirt.core.POST
import invirt.core.httpSeeOther
import invirt.core.toForm
import invirt.core.views.errorResponse
import invirt.core.views.renderTemplate
import invirt.core.withCookies
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

object LoginHandler {

    operator fun invoke(authService: AuthenticationService): RoutingHttpHandler = routes(
        "/login" GET { renderTemplate("login") },

        "/login" POST { request ->
            val loginForm = request.toForm<LoginForm>()
            val tokens = authService.login(loginForm.email, loginForm.password)
            if (tokens != null) {
                // Successfully logged in, so redirect to dashboard
                httpSeeOther("/dashboard")
                    .withCookies(tokens.cookies())
            } else {
                // Login failed so return to login and display an error message
                errorResponse(
                    "login",
                    "credentials" to "We could not find these credentials"
                )
            }
        }
    )
}

private class LoginForm(
    val email: String,
    val password: String
)
