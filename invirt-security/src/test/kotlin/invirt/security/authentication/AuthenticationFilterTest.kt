package invirt.security.authentication

import invirt.core.Invirt
import invirt.security.TestPrincipal
import invirt.security.authTestRoute
import invirt.security.test.failingAuthenticator
import invirt.security.test.successAuthenticator
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.kotest.shouldHaveSetCookie

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
        val principal = TestPrincipal(uuid7())
        val authenticator = successAuthenticator(principal)
        AuthenticationFilter(authenticator)
            .authTestRoute()
            .shouldHavePrincipal(principal)
    }

    "newCookies updates cookies" {
        val principal = TestPrincipal(uuid7())

        val authenticator = successAuthenticator(principal, listOf(Cookie("test-cookie", "refreshed-value")))
        AuthenticationFilter(authenticator)
            .authTestRoute()
            .shouldHavePrincipal(principal)
            .response shouldHaveSetCookie Cookie("test-cookie", "refreshed-value")
    }
})
