package invirt.http4k.security

import invirt.http4k.GET
import invirt.http4k.security.authentication.AuthenticationResponse
import invirt.http4k.security.authentication.Authenticator
import invirt.http4k.security.authentication.Principal
import invirt.http4k.security.authentication.principal
import invirt.http4k.security.authentication.useOnThisThread
import invirt.utils.uuid7
import io.kotest.matchers.shouldBe
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.then
import org.http4k.routing.routes

val failingAuthenticator: Authenticator = object : Authenticator {
    override fun authenticate(request: Request) = AuthenticationResponse.Unauthenticated
}

fun successAuthenticator(principal: Principal, newCookies: List<Cookie> = emptyList()): Authenticator = object : Authenticator {
    override fun authenticate(request: Request) = AuthenticationResponse.Authenticated(principal, newCookies)
}

data class TestPrincipal(
    val id: String,
    val attributes: Map<String, Any> = emptyMap()
) : Principal

fun withRoles(vararg roles: String, block: () -> Unit) {
    TestPrincipal(uuid7(), attributes = mapOf("roles" to roles.toSet())).useOnThisThread {
        block()
    }
}

val Principal.roles: Set<String> get() = (this as TestPrincipal).attributes["roles"]?.let { it as Set<String> } ?: emptySet()

fun Filter.authTestRoute(): AuthTestResult {
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
    return AuthTestResult(threadPrincipal, requestPrincipal, response)
}

data class AuthTestResult(
    val threadPrincipal: Principal?,
    val requestPrincipal: Principal?,
    val response: Response
) {
    infix fun shouldBePrincipal(principal: Principal): AuthTestResult {
        this.threadPrincipal shouldBe principal
        this.requestPrincipal shouldBe principal
        return this
    }

    fun shouldBeNull(): AuthTestResult {
        this.threadPrincipal shouldBe null
        this.requestPrincipal shouldBe null
        return this
    }
}
