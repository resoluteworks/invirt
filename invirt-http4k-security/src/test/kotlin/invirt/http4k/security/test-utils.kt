package invirt.http4k.security

import invirt.http4k.security.authentication.AuthTokens
import invirt.http4k.security.authentication.Authentication
import invirt.http4k.security.authentication.AuthenticationResponse
import invirt.http4k.security.authentication.Authenticator
import invirt.http4k.security.authentication.Principal
import invirt.utils.uuid7
import org.http4k.core.Request
import org.http4k.core.cookie.Cookie

val failingAuthenticator: Authenticator = object : Authenticator {
    override fun authenticate(request: Request) = AuthenticationResponse.Unauthenticated
    override fun logout(request: Request) {}
}

fun successAuthenticator(principal: Principal, tokens: AuthTokens): Authenticator = object : Authenticator {
    override fun authenticate(request: Request) = AuthenticationResponse.Authenticated(Authentication(principal, tokens))
    override fun logout(request: Request) {}
}

data class TestPrincipal(
    val id: String,
    val attributes: Map<String, Any> = emptyMap()
) : Principal

data class TestTokens(val cookies: List<Cookie> = emptyList()) : AuthTokens {

    constructor(vararg cookies: Cookie) : this(cookies.toList())

    override fun responseCookies(): List<Cookie> = cookies
}

fun withRoles(vararg roles: String, block: () -> Unit) {
    TestPrincipal(uuid7(), attributes = mapOf("roles" to roles.toSet())).useOnThisThread {
        block()
    }
}

val Principal.roles: Set<String> get() = (this as TestPrincipal).attributes["roles"]?.let { it as Set<String> } ?: emptySet()
