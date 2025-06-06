package invirt.core

import invirt.test.shouldBeRedirectTo
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
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
        httpHandler(Request(Method.GET, "/test")).bodyString()
            .trim() shouldBe """<html><head><meta http-equiv="refresh" content="0;URL='/other'"/></head></html>"""
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
