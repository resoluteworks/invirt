package invirt.http4k.security.authentication

import invirt.http4k.AppRequestContexts
import org.http4k.core.Request
import org.http4k.lens.RequestContextKey

private val authenticationContextKey = RequestContextKey.optional<Authentication<*, *>>(AppRequestContexts.contexts)

data class Authentication<P : Principal, T : AuthTokens>(val principal: P, val tokens: T) {
    companion object {
        internal val current: Authentication<*, *>? get() = authThreadLocal.get()
    }
}

internal var Request.authentication: Authentication<*, *>?
    get() = authenticationContextKey[this]
    set(auth) {
        auth?.let {
            authThreadLocal.set(auth)
        } ?: authThreadLocal.remove()
        authenticationContextKey[this] = auth
    }

val Request.principal: Principal? get() = this.authentication?.principal
