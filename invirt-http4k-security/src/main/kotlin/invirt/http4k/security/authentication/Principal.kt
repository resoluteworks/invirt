package invirt.http4k.security.authentication

import invirt.utils.withValue

internal val authThreadLocal = ThreadLocal<Authentication<*, *>>()

interface Principal {

    fun <T> useOnThisThread(block: (Principal) -> T): T {
        val authentication = Authentication(this, AuthTokens { emptyList() })
        return authThreadLocal.withValue(authentication) {
            block(this)
        }
    }

    companion object {
        val currentSafe: Principal? get() = Authentication.current?.principal
        val current: Principal get() = Authentication.current!!.principal
        val present: Boolean get() = currentSafe != null

        inline fun <reified P : Principal> current(): P {
            return current as P
        }
    }
}
