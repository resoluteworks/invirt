package invirt.http4k.security.authentication

import org.http4k.core.cookie.Cookie

fun interface AuthTokens {
    fun responseCookies(): List<Cookie>
}
