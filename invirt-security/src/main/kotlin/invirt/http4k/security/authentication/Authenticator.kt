package invirt.http4k.security.authentication

import org.http4k.core.Request
import org.http4k.core.cookie.Cookie

interface Authenticator {
    /**
     * Authenticates an HTTP request, usually by extracting headers/cookies and returning them as AuthTokens
     * along with the authenticated Principal.
     */
    fun authenticate(request: Request): AuthenticationResponse

    /**
     * Removes persistent session/auth state, or invalidates tokens
     */
    fun logout(request: Request)
}

sealed class AuthenticationResponse {

    data object Unauthenticated : AuthenticationResponse()

    data class Authenticated<P : Principal>(
        val principal: P,
        val newCookies: List<Cookie> = emptyList()
    ) : AuthenticationResponse()
}
