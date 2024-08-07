package invirt.http4k.security.authentication

import invirt.http4k.InvirtFilter
import invirt.http4k.security.TestPrincipal
import invirt.http4k.security.authTestRoute
import invirt.http4k.security.failingAuthenticator
import invirt.http4k.security.successAuthenticator
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.then
import org.http4k.kotest.shouldHaveSetCookie

class AuthenticationFilterTest : StringSpec({

    "unauthenticated" {
        InvirtFilter()
            .then(AuthenticationFilter(failingAuthenticator))
            .authTestRoute()
            .shouldHaveNullPrincipal()
            .response.cookies().shouldBeEmpty() // No cookies set
    }

    "principal present when authenticated" {
        val principal = TestPrincipal(uuid7())
        val authenticator = successAuthenticator(principal)
        InvirtFilter()
            .then(AuthenticationFilter(authenticator))
            .authTestRoute()
            .shouldHavePrincipal(principal)

        // Authentication always cleared after request finishes
        Principal.isPresent shouldBe false
    }

    "newCookies updates cookies" {
        val principal = TestPrincipal(uuid7())

        val authenticator = successAuthenticator(principal, listOf(Cookie("test-cookie", "refreshed-value")))
        InvirtFilter()
            .then(AuthenticationFilter(authenticator))
            .authTestRoute()
            .shouldHavePrincipal(principal)
            .response shouldHaveSetCookie Cookie("test-cookie", "refreshed-value")
    }
})
