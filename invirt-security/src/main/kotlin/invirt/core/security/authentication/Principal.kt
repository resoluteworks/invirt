package invirt.core.security.authentication

import invirt.http4k.InvirtRequestContext
import invirt.utils.threads.withValue
import org.http4k.core.Request
import org.http4k.lens.RequestContextKey

internal val principalThreadLocal = ThreadLocal<Principal>()
internal val principalContextKey = RequestContextKey.optional<Principal>(InvirtRequestContext.http4kRequestContexts)

interface Principal {

    companion object {
        /**
         * Returns the [Principal] on the current thread if present, `null` otherwise
         */
        val currentSafe: Principal? get() = principalThreadLocal.get()

        /**
         * Returns the [Principal] on the current thread if present, fails otherwise
         */
        val current: Principal get() = currentSafe ?: throw IllegalStateException("No Principal found on current threads")

        /**
         * Checks if a [Principal] is present on the current thread.
         */
        val isPresent: Boolean get() = principalThreadLocal.get() != null
    }
}

fun <T> Principal.useOnThisThread(block: (Principal) -> T): T = principalThreadLocal.withValue(this) {
    block(this)
}

fun <T> Principal.useOnThreadAndRequest(request: Request, block: (Principal) -> T): T {
    try {
        principalThreadLocal.set(this)
        request.setPrincipal(this)
        return block(this)
    } finally {
        principalThreadLocal.remove()
        request.clearPrincipal()
    }
}

fun Request.setPrincipal(principal: Principal) {
    principalContextKey[this] = principal
}

fun Request.clearPrincipal() {
    principalContextKey[this] = null
}

val Request.principal: Principal? get() = principalContextKey[this]
