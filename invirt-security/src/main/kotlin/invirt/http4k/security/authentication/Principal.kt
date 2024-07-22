package invirt.http4k.security.authentication

import invirt.http4k.InvirtFilter
import invirt.utils.withValue
import org.http4k.core.Request
import org.http4k.lens.RequestContextKey

internal val principalThreadLocal = ThreadLocal<Principal>()
internal val principalContextKey = RequestContextKey.optional<Principal>(InvirtFilter.requestContexts)

interface Principal {

    companion object {
        val currentSafe: Principal? get() = principalThreadLocal.get()
        val current: Principal get() = currentSafe!!
        val present: Boolean get() = principalThreadLocal.get() != null
        inline fun <reified P : Principal> current(): P = current as P
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
