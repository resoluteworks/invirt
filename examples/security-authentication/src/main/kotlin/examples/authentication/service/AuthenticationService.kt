package examples.authentication.service

import examples.authentication.service.Tokens.Companion.COOKIE_JWT
import examples.authentication.service.Tokens.Companion.COOKIE_SESSION_ID
import examples.authentication.service.Tokens.Companion.JWT_EXPIRY_MINUTES
import examples.authentication.service.Tokens.Companion.SESSION_EXPIRY_MINUTES
import examples.authentication.service.Tokens.Companion.jwtCookie
import examples.authentication.service.Tokens.Companion.sessionCookie
import invirt.http4k.security.authentication.AuthenticationResponse
import invirt.http4k.security.authentication.Authenticator
import invirt.http4k.security.authentication.Principal
import invirt.http4k.withCookies
import invirt.utils.uuid7
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidate
import java.time.Clock
import java.time.Instant

class AuthenticationService(
    // Mainly used for testing
    private val clock: Clock = Clock.systemUTC()
) : Authenticator {
    private val users = listOf(
        User("user@test.com", "test", "USER"),
        User("admin@test.com", "test", "ADMIN")
    )

    private val usersById = users.associateBy { it.id }
    private val usersByEmail = users.associateBy { it.email }

    internal val sessions = mutableMapOf<String, Session>()

    fun login(email: String, password: String): Tokens? {
        val user = usersByEmail[email]
        if (user == null || user.password != password) {
            return null
        }

        val session = Session(user.id, SESSION_EXPIRY_MINUTES)
        sessions[session.id] = session
        return Tokens(accessToken(user.email, user.role, JWT_EXPIRY_MINUTES), session.id)
    }

    fun invalidateCookies(response: Response): Response = response.withCookies(
        listOf(
            jwtCookie("").invalidate(),
            sessionCookie("").invalidate()
        )
    )

    override fun authenticate(request: Request): AuthenticationResponse {
        val sessionId = request.cookie(COOKIE_SESSION_ID)?.value
            ?: return AuthenticationResponse.Unauthenticated

        val jwt = request.cookie(COOKIE_JWT)?.value

        if (jwt != null) {
            val jwtVerification = verifyJwt(jwt, clock)
            if (jwtVerification is TokenVerification.Success) {
                // The access JWT token is still valid, return a Principal (User)
                return AuthenticationResponse.Authenticated(usersByEmail[jwtVerification.jwt.subject]!!)
            }
        }

        // The session might still be valid even if the JWT is expired
        return getUser(sessionId)?.let { user ->
            // If the session is valid re-issue the JWT to be set as a cookie
            val newJwt = accessToken(user.email, user.role, JWT_EXPIRY_MINUTES)
            AuthenticationResponse.Authenticated(user, listOf(Tokens.jwtCookie(newJwt)))
        } ?: AuthenticationResponse.Unauthenticated
    }

    private fun getUser(sessionId: String): User? {
        val session = sessions[sessionId] ?: return null
        val user = usersById[session.userId] ?: return null

        if (session.isExpired(clock)) {
            // If the session is expired remove it
            sessions.remove(sessionId)
            return null
        } else {
            // Otherwise "touch" the session to extend its expiry
            sessions[sessionId] = session.touch(SESSION_EXPIRY_MINUTES)
            return user
        }
    }
}

data class Tokens(
    val jwt: String,
    val sessionId: String
) {

    fun cookies(): List<Cookie> = listOf(jwtCookie(jwt), sessionCookie(sessionId))

    companion object {
        internal const val JWT_EXPIRY_MINUTES = 5L
        internal const val SESSION_EXPIRY_MINUTES = 60L

        internal const val COOKIE_JWT = "auth-jwt"
        internal const val COOKIE_SESSION_ID = "auth-session-id"

        internal fun jwtCookie(jwt: String) = Cookie(
            name = COOKIE_JWT,
            value = jwt,
            httpOnly = true,
            sameSite = SameSite.Strict,
            expires = Instant.now().plusSeconds(JWT_EXPIRY_MINUTES * 60),
            path = "/"
        )

        internal fun sessionCookie(sessionId: String) = Cookie(
            name = COOKIE_SESSION_ID,
            value = sessionId,
            httpOnly = true,
            sameSite = SameSite.Strict,
            expires = Instant.now().plusSeconds(SESSION_EXPIRY_MINUTES * 60),
            path = "/"
        )
    }
}

data class Session(
    val userId: String,
    val expiresAt: Instant,
    val id: String = uuid7()
) {
    constructor(userId: String, sessionExpiryMinutes: Long) : this(
        userId = userId,
        expiresAt = Instant.now().plusSeconds(60 * sessionExpiryMinutes)
    )

    fun isExpired(clock: Clock): Boolean = Instant.now(clock).isAfter(expiresAt)

    fun touch(sessionExpiryMinutes: Long): Session =
        this.copy(
            userId = userId,
            expiresAt = Instant.now().plusSeconds(60 * sessionExpiryMinutes)
        )
}

data class User(
    val email: String,
    val password: String,
    val role: String,
    val id: String = uuid7()
) : Principal
