package invirt.http4k.security.authentication

import invirt.utils.withValue

internal val principalThreadLocal = ThreadLocal<Principal>()

interface Principal {

    companion object {
        val current: Principal get() = principalThreadLocal.get()!!
        val present: Boolean get() = principalThreadLocal.get() != null
        inline fun <reified P : Principal> current(): P = current as P
    }
}

fun <T> Principal.useOnThisThread(block: (Principal) -> T): T = principalThreadLocal.withValue(this) {
    block(this)
}
