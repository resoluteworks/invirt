package invirt.core

import invirt.test.shouldBeHtmlRedirectTo
import invirt.test.shouldBeRedirectTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.format.Jackson.json
import org.http4k.kotest.shouldHaveSetCookie
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Instant
import java.time.temporal.ChronoUnit

class ResponseTest : StringSpec({

    "bidilens.ok" {
        data class JsonTestPojo(
            val name: String,
            val enabled: Boolean
        )

        val httpHandler = routes(
            "/test" bind Method.GET to {
                Response(Status.OK).json(JsonTestPojo("Apache Productions", true))
            }
        )

        httpHandler(
            Request(Method.GET, "/test")
        ).bodyString() shouldBe """{"name":"Apache Productions","enabled":true}"""
    }

    "withCookies" {
        val expiry = Instant.now().plusSeconds(30).truncatedTo(ChronoUnit.SECONDS)
        val response = Response(Status.OK)
            .withCookies(
                listOf(
                    Cookie(name = "one", value = "1", expires = expiry, secure = true, httpOnly = true),
                    Cookie(name = "two", value = "2", expires = expiry, secure = false, httpOnly = false)
                )
            )

        response shouldHaveSetCookie Cookie(
            name = "one",
            value = "1",
            expires = expiry,
            secure = true,
            httpOnly = true
        )
        response shouldHaveSetCookie Cookie(
            name = "two",
            value = "2",
            expires = expiry,
            secure = false,
            httpOnly = false
        )
    }

    "withCookiesIfAbsent" {
        val response = Response(Status.OK)
            .withCookies(listOf(Cookie(name = "session", value = "handler-value")))
            .withCookiesIfAbsent(
                listOf(
                    Cookie(name = "session", value = "refreshed-value"),
                    Cookie(name = "locale", value = "en-GB")
                )
            )

        // A name the response already carries is skipped rather than appended - a second Set-Cookie
        // for the same name would win in the browser and undo what the handler set
        response.cookies() shouldHaveSize 2
        response shouldHaveSetCookie Cookie(name = "session", value = "handler-value")
        response shouldHaveSetCookie Cookie(name = "locale", value = "en-GB")
    }

    "withCookiesIfAbsent matches on name only, ignoring path and domain" {
        val response = Response(Status.OK)
            .withCookies(listOf(Cookie(name = "session", value = "handler-value", path = "/admin")))
            .withCookiesIfAbsent(listOf(Cookie(name = "session", value = "refreshed-value", path = "/")))

        response.cookies() shouldHaveSize 1
        response shouldHaveSetCookie Cookie(name = "session", value = "handler-value", path = "/admin")
    }

    "invalidateCookies" {
        val expiry = Instant.now().plusSeconds(30)
        val cookies = listOf(
            Cookie(name = "one", value = "1", expires = expiry, secure = true, httpOnly = true),
            Cookie(name = "two", value = "2", expires = expiry, secure = false, httpOnly = false)
        )
        val response = Response(Status.OK)
            .withCookies(cookies)
            .invalidateCookies(cookies)

        response shouldHaveSetCookie Cookie(
            name = "one",
            value = "",
            expires = Instant.EPOCH,
            maxAge = 0,
            secure = true,
            httpOnly = true
        )
        response shouldHaveSetCookie Cookie(
            name = "two",
            value = "",
            expires = Instant.EPOCH,
            maxAge = 0,
            secure = false,
            httpOnly = false
        )
    }

    "htmlRedirect" {
        val httpHandler = routes(
            "/test" GET { htmlRedirect("/other") }
        )
        httpHandler(Request(Method.GET, "/test")).shouldBeHtmlRedirectTo("/other")
    }

    "turboStream" {
        val httpHandler = routes("/test" GET { Response(Status.OK).turboStream() })
        httpHandler(Request(Method.GET, "/test")).header("Content-Type") shouldBe "text/vnd.turbo-stream.html"
    }

    "httpSeeOther" {
        val httpHandler = routes("/test" GET { httpSeeOther("/something/else") })
        httpHandler(Request(Method.GET, "/test")).shouldBeRedirectTo("/something/else")
    }

    "httpNotFound" {
        val httpHandler = routes("/test" GET { httpNotFound() })
        httpHandler(Request(Method.GET, "/test")) shouldHaveStatus Status.NOT_FOUND
    }

    "turboStreamRefresh" {
        val httpHandler = routes("/test" GET { turboStreamRefresh() })
        val response = httpHandler(Request(Method.GET, "/test"))
        response.header("Content-Type") shouldBe "text/vnd.turbo-stream.html"
        response.bodyString().trim() shouldBe """<turbo-stream action="refresh"></turbo-stream>"""
    }
})
