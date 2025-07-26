package invirt.security.test

import invirt.security.authentication.AuthenticationResponse
import invirt.security.authentication.Authenticator
import invirt.security.authentication.Principal
import org.http4k.core.Request
import org.http4k.core.cookie.Cookie

val failingAuthenticator: Authenticator = object : Authenticator {
    override fun authenticate(request: Request) = AuthenticationResponse.Unauthenticated
}

fun successAuthenticator(principal: Principal, newCookies: List<Cookie> = emptyList()): Authenticator = object : Authenticator {
    override fun authenticate(request: Request): AuthenticationResponse.Authenticated<Principal> = if (newCookies.isNotEmpty()) {
        AuthenticationResponse.Authenticated(principal, newCookies)
    } else {
        AuthenticationResponse.Authenticated(principal)
    }
}
