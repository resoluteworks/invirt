package invirt.http4k.security.authentication

import invirt.http4k.withCookies
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
                val response = authResponse.principal.useOnThreadAndRequest(request) {
                    next(request)
                }

                // Set cookies if any have been set by Authenticator
                if (authResponse.newCookies.isNotEmpty()) {
                    response.withCookies(authResponse.newCookies)
                } else {
                    response
                }
            } else {
                next(request)
            }
        }
    }
}
