package invirt.security.authentication

import invirt.core.withCookiesIfAbsent
import org.http4k.core.Filter

/**
 * Filter for setting the Principal on the current request and current thread from a request's headers/cookies.
 * It doesn't stop the current request when un-authenticated, so it's the responsibility
 * of the underlying filters/handlers to check the presence of a Principal and/or its permissions according
 * to application requirements.
 */
object AuthenticationFilter {

    operator fun invoke(authenticator: Authenticator): Filter = Filter { next ->
        { request ->
            val authResponse = authenticator.authenticate(request)
            if (authResponse is AuthenticationResponse.Authenticated<*>) {
                val response = next(request.withPrincipal(authResponse.principal))

                // Set cookies if any have been set by Authenticator, but never over a cookie of the same
                // name that the handler itself set - a sign-out invalidating the session cookie has to
                // beat a token refreshed on the way in, or the user stays signed in.
                if (authResponse.newCookies.isNotEmpty()) {
                    response.withCookiesIfAbsent(authResponse.newCookies)
                } else {
                    response
                }
            } else {
                next(request)
            }
        }
    }
}
