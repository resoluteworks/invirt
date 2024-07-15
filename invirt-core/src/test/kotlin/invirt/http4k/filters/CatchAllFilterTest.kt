package invirt.http4k.filters

import invirt.http4k.GET
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class CatchAllFilterTest : StringSpec({

    "exception to status" {
        val handler = CatchAll(
            mapOf(
                IllegalArgumentException::class to Status.BAD_REQUEST,
                ClassCastException::class to Status.NOT_FOUND
            )
        )
            .then(
                routes(
                    "/illegal-argument" GET { throw IllegalArgumentException("Error") },
                    "/class-cast" GET { throw ClassCastException("Error") },
                    "/internal" GET { throw RuntimeException("Error") }
                )
            )

        handler(Request(Method.GET, "/illegal-argument")) shouldHaveStatus Status.BAD_REQUEST
        handler(Request(Method.GET, "/class-cast")) shouldHaveStatus Status.NOT_FOUND
        handler(Request(Method.GET, "/internal")) shouldHaveStatus Status.INTERNAL_SERVER_ERROR
    }
})
