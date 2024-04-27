package invirt.http4k.security.handlers

import invirt.http4k.AppRequestContexts
import invirt.http4k.security.TestPrincipal
import invirt.http4k.security.TestTokens
import invirt.http4k.security.authentication.AuthenticationFilter
import invirt.http4k.security.successAuthenticator
import invirt.test.shouldBeRedirectTo
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.then
import org.http4k.kotest.shouldHaveSetCookie
import org.http4k.kotest.shouldHaveStatus
import java.time.Instant

class LogoutHandlerTest : StringSpec({

    "logout" {
        val principal = TestPrincipal(uuid7())
        val authenticator = successAuthenticator(principal, TestTokens(Cookie("name", "value")))

        val response = AppRequestContexts().then(AuthenticationFilter(authenticator))
            .then(LogoutHandler(authenticator, "/logged-out"))
            .invoke(Request(Method.GET, "/logout"))
        response shouldHaveStatus Status.SEE_OTHER
        response shouldHaveSetCookie Cookie("name", "deleted", expires = Instant.EPOCH)
        response shouldBeRedirectTo "/logged-out"
    }

    "logout - custom uri, POST" {
        val principal = TestPrincipal(uuid7())
        val authenticator = successAuthenticator(principal, TestTokens(Cookie("name", "value")))

        val response = AppRequestContexts().then(AuthenticationFilter(authenticator))
            .then(LogoutHandler(authenticator, "/logged-out", "/auth/logout", Method.POST))
            .invoke(Request(Method.POST, "/auth/logout"))
        response shouldHaveStatus Status.SEE_OTHER
        response shouldHaveSetCookie Cookie("name", "deleted", expires = Instant.EPOCH)
        response shouldBeRedirectTo "/logged-out"
    }
})
