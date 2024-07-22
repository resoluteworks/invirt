package examples.authentication.service

import examples.authentication.service.Tokens.Companion.SESSION_EXPIRY_MINUTES
import invirt.http4k.security.authentication.AuthenticationResponse
import invirt.test.cookies
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.clock.TestClock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.date.shouldBeCloseTo
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.toKotlinDuration

class AuthenticationServiceTest : StringSpec({

    "login success" {
        val tokens = AuthenticationService().login("user@test.com", "test")
        tokens shouldNotBe null
        (verifyJwt(tokens!!.jwt) as TokenVerification.Success).jwt.expiresAt.toInstant()
            .shouldBeCloseTo(Instant.now().plusSeconds(60 * 5), Duration.ofMillis(1000).toKotlinDuration())
    }

    "login failure" {
        AuthenticationService().login("user@test.com", "aaa") shouldBe null
        AuthenticationService().login("admin@test.com", "bbb") shouldBe null
    }

    "authenticate - reject when no cookies present" {
        AuthenticationService().authenticate(Request(Method.GET, "/test")) shouldBe AuthenticationResponse.Unauthenticated
    }

    "authenticate - reject when session doesn't exist" {
        AuthenticationService().authenticate(
            Request(Method.GET, "/test")
                .cookie(
                    Cookie(
                        name = "auth-session-id",
                        value = uuid7(),
                        httpOnly = true,
                        sameSite = SameSite.Strict,
                        expires = Instant.now().plusSeconds(SESSION_EXPIRY_MINUTES * 60),
                        path = "/"
                    )
                )
        ) shouldBe AuthenticationResponse.Unauthenticated
    }

    "authenticate - reject when session is expired" {
        val clock = TestClock(Instant.now(), ZoneOffset.UTC)

        val authenticationService = AuthenticationService(clock)
        val cookies = authenticationService.login("user@test.com", "test")!!.cookies()

        clock.plus(Duration.ofMinutes(61).toKotlinDuration())

        authenticationService.authenticate(Request(Method.GET, "/test").cookies(cookies))
            .shouldBeTypeOf<AuthenticationResponse.Unauthenticated>()
        authenticationService.sessions.shouldBeEmpty()
    }

    "authenticate - success" {
        val authenticationService = AuthenticationService()
        val cookies = authenticationService.login("user@test.com", "test")!!.cookies()
        val result = authenticationService.authenticate(Request(Method.GET, "/test").cookies(cookies))
        result.shouldBeTypeOf<AuthenticationResponse.Authenticated<*>>()
        result.newCookies.shouldBeEmpty()
    }

    "authenticate - success whe jwt is expired, new JTW issued" {
        val clock = TestClock(Instant.now(), ZoneOffset.UTC)

        val authenticationService = AuthenticationService(clock)
        val cookies = authenticationService.login("user@test.com", "test")!!.cookies()

        clock.plus(Duration.ofMinutes(10).toKotlinDuration())

        val result = authenticationService.authenticate(Request(Method.GET, "/test").cookies(cookies))
        result.shouldBeTypeOf<AuthenticationResponse.Authenticated<*>>()
        result.newCookies.size shouldBe 1
        result.newCookies.first().expires!!.shouldBeCloseTo(Instant.now().plusSeconds(60 * 5), Duration.ofMillis(1000).toKotlinDuration())
    }
})
