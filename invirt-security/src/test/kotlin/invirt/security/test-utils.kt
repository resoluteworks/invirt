package invirt.security

import invirt.core.GET
import invirt.security.authentication.Principal
import invirt.security.authentication.principal
import io.kotest.matchers.shouldBe
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.routes

data class TestPrincipal(
    val id: String,
    val attributes: Map<String, Any> = emptyMap()
) : Principal

val TestPrincipal.roles: Set<String> get() = this.attributes["roles"]?.let { it as Set<String> } ?: emptySet()

fun Filter.authTestRoute(): AuthTestResult {
    var requestPrincipal: Principal? = null

    val httpHandler = this.then(
        routes(
            "/test" GET {
                requestPrincipal = it.principal
                Response(Status.OK)
            }
        )
    )
    val response = httpHandler(Request(Method.GET, "/test"))
    return AuthTestResult(requestPrincipal, response)
}

data class AuthTestResult(
    val requestPrincipal: Principal?,
    val response: Response
) {
    infix fun shouldHavePrincipal(principal: Principal): AuthTestResult {
        this.requestPrincipal shouldBe principal
        return this
    }

    fun shouldHaveNullPrincipal(): AuthTestResult {
        this.requestPrincipal shouldBe null
        return this
    }
}
