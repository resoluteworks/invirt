package invirt.http4k.security.authentication

import invirt.http4k.GET
import invirt.http4k.InvirtRequestContext
import invirt.http4k.security.TestPrincipal
import invirt.http4k.security.TestTokens
import invirt.http4k.security.failingAuthenticator
import invirt.http4k.security.successAuthenticator
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.kotest.shouldHaveSetCookie
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes
import java.time.Instant

class AuthenticationFilterTest : StringSpec({

    "unauthenticated" {
        val response = InvirtRequestContext().then(AuthenticationFilter(failingAuthenticator)).expectPrincipal(null)
        // No cookies set
        response.cookies().shouldBeEmpty()
    }

    "principal present and cookies set when authenticated" {
        val principal = TestPrincipal(uuid7())
        val authenticator = successAuthenticator(principal, TestTokens(Cookie("test-cookie", "value")))
        val response = InvirtRequestContext().then(AuthenticationFilter(authenticator)).expectPrincipal(principal)

        // Cookies set when authenticated
        response shouldHaveSetCookie Cookie("test-cookie", "value")

        // Authentication always cleared after request finishes
        Authentication.current shouldBe null
        Principal.currentSafe shouldBe null
    }

    "underlying filter removing authentication" {
        val principal = TestPrincipal(uuid7())
        val tokens = TestTokens(Cookie("test-cookie", "value"))
        val authenticator = successAuthenticator(principal, tokens)
        val httpHandler = InvirtRequestContext().then(AuthenticationFilter(authenticator))
            .then(
                Filter { next ->
                    { request ->
                        // Auth is still present at this time
                        Authentication.current shouldBe Authentication(principal, tokens)

                        request.authentication = null

                        Authentication.current shouldBe null
                        request.authentication shouldBe null
                        next(request)
                    }
                }
            )

        val response = httpHandler.expectPrincipal(null)
        response shouldHaveSetCookie Cookie("test-cookie", "deleted", expires = Instant.EPOCH)
    }

    "underlying filter refreshes authentication" {
        val principal = TestPrincipal(uuid7())
        val tokens = TestTokens(Cookie("test-cookie", "value"))

        val freshPrincipal = TestPrincipal(uuid7())
        val freshTokens = TestTokens(Cookie("test-cookie", "refreshed-value"))

        val authenticator = successAuthenticator(principal, tokens)
        val httpHandler = InvirtRequestContext().then(AuthenticationFilter(authenticator))
            .then(
                Filter { next ->
                    { request ->
                        // Auth is still the old one at this time
                        Authentication.current shouldBe Authentication(principal, tokens)

                        request.authentication = Authentication(freshPrincipal, freshTokens)

                        Authentication.current shouldBe Authentication(freshPrincipal, freshTokens)
                        request.authentication shouldBe Authentication(freshPrincipal, freshTokens)

                        next(request)
                    }
                }
            )

        val response = httpHandler.expectPrincipal(freshPrincipal)
        response shouldHaveStatus Status.OK
        response shouldHaveSetCookie Cookie("test-cookie", "refreshed-value")
    }

    "underlying handler removing authentication" {
        val principal = TestPrincipal(uuid7())
        val tokens = TestTokens(Cookie("test-cookie", "value"))

        val authenticator = successAuthenticator(principal, tokens)
        val httpHandler = InvirtRequestContext().then(AuthenticationFilter(authenticator))
            .then(
                routes(
                    "/test" GET { request ->
                        // Auth is still present at this time
                        Authentication.current shouldBe Authentication(principal, tokens)

                        request.authentication = null

                        Authentication.current shouldBe null
                        request.authentication shouldBe null
                        Principal.currentSafe shouldBe null
                        request.principal shouldBe null
                        Response(Status.OK)
                    }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))
        response shouldHaveStatus Status.OK
        response shouldHaveSetCookie Cookie("test-cookie", "deleted", expires = Instant.EPOCH)
    }

    "underlying handlers refreshes authentication" {
        val principal = TestPrincipal(uuid7())
        val tokens = TestTokens(Cookie("test-cookie", "value"))

        val freshPrincipal = TestPrincipal(uuid7())
        val freshTokens = TestTokens(Cookie("test-cookie", "refreshed-value"))

        val authenticator = successAuthenticator(principal, tokens)
        val httpHandler = InvirtRequestContext().then(AuthenticationFilter(authenticator))
            .then(
                routes(
                    "/test" GET { request ->
                        // Auth is still the old one at this time
                        Authentication.current shouldBe Authentication(principal, tokens)

                        request.authentication = Authentication(freshPrincipal, freshTokens)

                        Authentication.current shouldBe Authentication(freshPrincipal, freshTokens)
                        request.authentication shouldBe Authentication(freshPrincipal, freshTokens)
                        Principal.current shouldBe freshPrincipal
                        request.principal shouldBe freshPrincipal
                        Response(Status.OK)
                    }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))
        response shouldHaveStatus Status.OK
        response shouldHaveSetCookie Cookie("test-cookie", "refreshed-value")
    }
})

fun Filter.expectPrincipal(principal: Principal?): Response {
    var threadPrincipal: Principal? = null
    var requestPrincipal: Principal? = null

    val httpHandler = this.then(
        routes(
            "/test" GET {
                threadPrincipal = Principal.currentSafe
                requestPrincipal = it.principal
                Response(Status.OK)
            }
        )
    )
    val response = httpHandler(Request(Method.GET, "/test"))

    principal?.let {
        threadPrincipal!! shouldBe principal
        requestPrincipal!! shouldBe principal
    } ?: {
        threadPrincipal shouldBe null
        requestPrincipal shouldBe null
    }

    return response
}
