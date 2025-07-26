package invirt.security

import invirt.core.GET
import invirt.security.authentication.Principal
import invirt.security.authentication.principal
import invirt.security.authentication.useOnThisThread
import invirt.utils.uuid7
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
    infix fun shouldHavePrincipal(principal: Principal): AuthTestResult {
        this.threadPrincipal shouldBe principal
        this.requestPrincipal shouldBe principal
        return this
    }

    fun shouldHaveNullPrincipal(): AuthTestResult {
        this.threadPrincipal shouldBe null
        this.requestPrincipal shouldBe null
        return this
    }
}
