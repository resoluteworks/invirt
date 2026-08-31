package invirt.security.authentication

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.filters.ErrorPages
import invirt.core.invalidateCookies
import invirt.core.withCookies
import invirt.security.TestPrincipal
import invirt.security.authTestRoute
import invirt.security.test.failingAuthenticator
import invirt.security.test.successAuthenticator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.cookie.invalidate
import org.http4k.core.then
import org.http4k.kotest.shouldHaveSetCookie
import org.http4k.routing.routes

class AuthenticationFilterTest : StringSpec({

    beforeAny {
        Invirt.configure()
    }

    "unauthenticated" {
        Invirt.configure()
        AuthenticationFilter(failingAuthenticator)
            .authTestRoute()
            .shouldHaveNullPrincipal()
            .response.cookies().shouldBeEmpty() // No cookies set
    }

    "principal present when authenticated" {
        val principal = TestPrincipal()
        val authenticator = successAuthenticator(principal)
        AuthenticationFilter(authenticator)
            .authTestRoute()
            .shouldHavePrincipal(principal)
    }

    "newCookies updates cookies" {
        val principal = TestPrincipal()

        val authenticator = successAuthenticator(principal, listOf(Cookie("test-cookie", "refreshed-value")))
        val response = AuthenticationFilter(authenticator)
            .authTestRoute()
            .shouldHavePrincipal(principal)
            .response

        response shouldHaveSetCookie Cookie("test-cookie", "refreshed-value")
        response.cookies() shouldHaveSize 1
    }

    // The handler owns the response: a cookie it set must survive whatever the Authenticator refreshed on
    // the way in. Set-Cookie appends, and the browser keeps the last header for a given name, so a
    // refreshed session cookie added after a sign-out's invalidation silently signs the user back in.
    // Asserted on cookies() rather than shouldHaveSetCookie, which only inspects the first header of a
    // name and is therefore blind to a live cookie appended behind an invalidated one.
    "newCookies never overwrite a cookie set by the handler" {
        val principal = TestPrincipal()
        val sessionCookie = Cookie("session", "current-value")
        val authenticator = successAuthenticator(principal, listOf(Cookie("session", "refreshed-value")))

        val response = AuthenticationFilter(authenticator)
            .authTestRoute(Response(Status.OK).invalidateCookies(listOf(sessionCookie)))
            .shouldHavePrincipal(principal)
            .response

        response.cookies() shouldHaveSize 1
        response shouldHaveSetCookie sessionCookie.invalidate()
    }

    "newCookies are set alongside unrelated cookies set by the handler" {
        val principal = TestPrincipal()
        val handlerCookie = Cookie("locale", "en-GB")
        val authenticator = successAuthenticator(principal, listOf(Cookie("session", "refreshed-value")))

        val response = AuthenticationFilter(authenticator)
            .authTestRoute(Response(Status.OK).withCookies(listOf(handlerCookie)))
            .shouldHavePrincipal(principal)
            .response

        response.cookies() shouldHaveSize 2
        response shouldHaveSetCookie handlerCookie
        response shouldHaveSetCookie Cookie("session", "refreshed-value")
    }

    // ErrorPages renders a new response from inside this filter, so the handler's cookies only reach the
    // filter because ErrorPages carries its Set-Cookie headers across. Both halves are needed: lose either
    // and the refreshed cookie is the last one on a signed-out response, which signs the user back in.
    "a handler cookie carried through ErrorPages still beats a refreshed cookie" {
        val principal = TestPrincipal()
        val sessionCookie = Cookie("session", "current-value")
        val authenticator = successAuthenticator(principal, listOf(Cookie("session", "refreshed-value")))

        val httpHandler = AuthenticationFilter(authenticator)
            .then(ErrorPages(Status.NOT_FOUND to "error/404"))
            .then(
                routes(
                    "/test" GET { Response(Status.NOT_FOUND).invalidateCookies(listOf(sessionCookie)) }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString().trim() shouldBe "Page not found"
        response.cookies() shouldHaveSize 1
        response shouldHaveSetCookie sessionCookie.invalidate()
    }
})
