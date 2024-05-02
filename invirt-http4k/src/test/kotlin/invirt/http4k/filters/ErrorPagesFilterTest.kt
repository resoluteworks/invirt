package invirt.http4k.filters

import invirt.http4k.GET
import invirt.http4k.views.Views
import invirt.http4k.views.setDefaultViewLens
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.routing.routes

class ErrorPagesFilterTest : StringSpec({

    beforeAny { setDefaultViewLens(Views.Classpath("webapp/views")) }

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
})
