package invirt.security.authentication

import invirt.utils.threads.withValue
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.RequestKey

internal val principalThreadLocal = ThreadLocal<Principal>()
internal val principalContextKey = RequestKey.optional<Principal>("currentPrincipal")

interface Principal {

    companion object {
        /**
         * Returns the [Principal] on the current thread if present, `null` otherwise
         */
        val currentSafe: Principal? get() = principalThreadLocal.get()

        /**
         * Returns the [Principal] on the current thread if present, fails otherwise
         */
        val current: Principal get() = currentSafe ?: throw IllegalStateException("No Principal found on current thread")

        /**
         * Checks if a [Principal] is present on the current thread.
         */
        val isPresent: Boolean get() = principalThreadLocal.get() != null
    }
}

fun <T> Principal.useOnThisThread(block: (Principal) -> T): T = principalThreadLocal.withValue(this) {
    block(this)
}

fun Request.withPrincipal(principal: Principal): Request = this.with(principalContextKey of principal)

val Request.principal: Principal? get() = principalContextKey(this)

val Request.hasPrincipal: Boolean get() = principalContextKey(this) != null
