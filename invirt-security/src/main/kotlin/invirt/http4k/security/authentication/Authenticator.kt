package invirt.http4k.security.authentication

import org.http4k.core.Request
import org.http4k.core.cookie.Cookie

interface Authenticator {
    /**
     * Authenticates an HTTP request, usually by extracting headers/cookies and returning them as AuthTokens
     * along with the authenticated Principal.
     */
    fun authenticate(request: Request): AuthenticationResponse
}

sealed class AuthenticationResponse {

    /**
     * A response returned by [Authenticator] when the security credentials on the
     * request could not be authenticated
     */
    data object Unauthenticated : AuthenticationResponse()

    /**
     * Represents a successful authentication response.
     *
     * @param principal The [Principal] that was successfully authenticated
     * @param newCookies A set of cookies to be set on the response once the request completes.
     * This can be used to refresh JWT tokens, for example.
     */
    data class Authenticated<P : Principal>(
        val principal: P,
        val newCookies: List<Cookie> = emptyList()
    ) : AuthenticationResponse()
}
