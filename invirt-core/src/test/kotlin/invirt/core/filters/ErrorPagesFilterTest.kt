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

    // Only cookies come across. The handler's other headers describe a body that the error page replaced.
    "headers other than cookies are not carried onto the error page" {
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
        response.headerValues("Content-Type") shouldHaveSize 1
    }
})
