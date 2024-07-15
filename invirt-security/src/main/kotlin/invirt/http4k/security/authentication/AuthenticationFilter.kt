package invirt.http4k.security.authentication

import invirt.http4k.invalidateCookies
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
            if (authResponse is AuthenticationResponse.Authenticated<*, *>) {
                val authentication = authResponse.authentication
                request.authentication = authentication
                try {
                    val response = next(request)

                    // Reading authentication from the request, in order to allow underlying filters/handlers
                    // to clear the authentication (logout) or refresh auth tokens. When the current request is missing
                    // an Authentication it means the user is not authenticated anymore and the cookies can be cleared
                    request.authentication?.let {
                        response.withCookies(it.tokens.responseCookies())
                    } ?: response.invalidateCookies(authentication.tokens.responseCookies())
                } finally {
                    request.authentication = null
                }
            } else {
                next(request)
            }
        }
    }
}
