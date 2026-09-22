package invirt.core.filters

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.invalidateCookies
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.cookie.invalidate
import org.http4k.core.then
import org.http4k.kotest.shouldHaveSetCookie
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class ErrorPagesFilterTest : StringSpec({

    beforeAny {
        Invirt.configure()
    }

    "404 page" {
        val httpHandler = ErrorPages(Status.NOT_FOUND to "error/404")
            .then(
                routes(
                    "/test" GET { Response(Status.NOT_FOUND) }
                )
            )

        httpHandler(Request(Method.GET, "/test")).bodyString().trim() shouldBe "Page not found"
    }

    "forbidden mapped to not found and custom error page" {
        val httpHandler = ErrorPages(mapOf(Status.NOT_FOUND to "error/404"))
            .then(StatusOverride(Status.FORBIDDEN to Status.NOT_FOUND))
            .then(
                routes(
                    "/test" GET { Response(Status.FORBIDDEN) }
                )
            )

        httpHandler(Request(Method.GET, "/test")).bodyString().trim() shouldBe "Page not found"
    }

    // The error page is a new response, so anything the handler decided about cookies would be lost with
    // it. A sign-out that renders as an error page still signed the user out.
    "cookies set by the handler survive onto the error page" {
        val sessionCookie = Cookie("session", "current-value")
        val httpHandler = ErrorPages(Status.NOT_FOUND to "error/404")
            .then(
                routes(
                    "/test" GET { Response(Status.NOT_FOUND).invalidateCookies(listOf(sessionCookie)) }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString().trim() shouldBe "Page not found"
        response.cookies() shouldHaveSize 1
        response shouldHaveSetCookie sessionCookie.invalidate()
    }

    // Only cookies and the caching directive come across. The handler's other headers describe a body
    // that the error page replaced.
    "headers other than cookies and Cache-Control are not carried onto the error page" {
        val httpHandler = ErrorPages(Status.NOT_FOUND to "error/404")
            .then(
                routes(
                    "/test" GET {
                        Response(Status.NOT_FOUND)
                            .header("X-Handler", "handler-value")
                            .header("Content-Type", "application/json")
                            .body("""{"error":"not found"}""")
                    }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString().trim() shouldBe "Page not found"
        response.header("X-Handler") shouldBe null
        response.header("Cache-Control") shouldBe null
        response.headerValues("Content-Type") shouldHaveSize 1
    }

    // A handler that forbids caching a miss means the miss, not the body: a CDN given no directive on the
    // rendered 404 would pin it with its own defaults.
    "the handler's Cache-Control survives onto the error page" {
        val httpHandler = ErrorPages(Status.NOT_FOUND to "error/404")
            .then(
                routes(
                    "/test" GET { Response(Status.NOT_FOUND).header("Cache-Control", "no-store") }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString().trim() shouldBe "Page not found"
        response.header("Cache-Control") shouldBe "no-store"
    }

    "the handler's Cache-Control survives onto the fallback body" {
        val httpHandler = ErrorPages(Status.NOT_FOUND to "error/missing-template", fallbackBody = "<p>Fallback</p>")
            .then(
                routes(
                    "/test" GET { Response(Status.NOT_FOUND).header("Cache-Control", "no-store") }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString() shouldBe "<p>Fallback</p>"
        response.header("Cache-Control") shouldBe "no-store"
    }

    // The error page is rendered while the application is already handling an error, and rendering it can
    // fail in turn. The fallback is what stops that second failure reaching the browser as a stack trace.
    "the fallback body is served when the error page throws while rendering" {
        val httpHandler = ErrorPages(
            Status.INTERNAL_SERVER_ERROR to "error/throwing",
            fallbackBody = "<html><body>Something went wrong</body></html>"
        ).then(
            routes(
                "/test" GET { Response(Status.INTERNAL_SERVER_ERROR) }
            )
        )

        val response = httpHandler(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.INTERNAL_SERVER_ERROR
        response.bodyString() shouldBe "<html><body>Something went wrong</body></html>"
        response.header("Content-Type") shouldBe "text/html; charset=utf-8"
    }

    "the fallback body is served when the error page template does not exist" {
        val httpHandler = ErrorPages(
            mapOf(Status.NOT_FOUND to "error/not-a-template"),
            fallbackBody = "<html><body>Page not found</body></html>"
        ).then(
            routes(
                "/test" GET { Response(Status.NOT_FOUND) }
            )
        )

        val response = httpHandler(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.NOT_FOUND
        response.bodyString() shouldBe "<html><body>Page not found</body></html>"
    }

    "an error page that throws with no fallback body is an empty response with the original status" {
        val httpHandler = ErrorPages(Status.INTERNAL_SERVER_ERROR to "error/throwing")
            .then(
                routes(
                    "/test" GET { Response(Status.INTERNAL_SERVER_ERROR) }
                )
            )

        val response = httpHandler(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.INTERNAL_SERVER_ERROR
        response.bodyString() shouldBe ""
        response.header("Content-Type") shouldBe null
    }

    // The fallback replaces the page, not the handler's decisions: a sign-out whose error page fails to
    // render still signed the user out.
    "cookies set by the handler survive onto the fallback body" {
        val sessionCookie = Cookie("session", "current-value")
        val httpHandler = ErrorPages(
            Status.INTERNAL_SERVER_ERROR to "error/throwing",
            fallbackBody = "<html><body>Something went wrong</body></html>"
        ).then(
            routes(
                "/test" GET { Response(Status.INTERNAL_SERVER_ERROR).invalidateCookies(listOf(sessionCookie)) }
            )
        )

        val response = httpHandler(Request(Method.GET, "/test"))

        response.bodyString() shouldBe "<html><body>Something went wrong</body></html>"
        response.cookies() shouldHaveSize 1
        response shouldHaveSetCookie sessionCookie.invalidate()
    }

    // A status with no mapped view is passed through untouched, fallback or not.
    "a response with no mapped error page is untouched" {
        val httpHandler = ErrorPages(
            Status.NOT_FOUND to "error/404",
            fallbackBody = "<html><body>Something went wrong</body></html>"
        ).then(
            routes(
                "/test" GET { Response(Status.OK).body("all good") }
            )
        )

        val response = httpHandler(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.OK
        response.bodyString() shouldBe "all good"
    }
})
