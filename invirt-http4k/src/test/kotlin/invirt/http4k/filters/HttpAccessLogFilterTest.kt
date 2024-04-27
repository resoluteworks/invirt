package invirt.http4k.filters

import invirt.http4k.GET
import io.github.oshai.kotlinlogging.KLogger
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.routes

class HttpAccessLogFilterTest : StringSpec({

    "defaults" {
        mockkObject(HttpAccessLogFilter) {
            val log = mockk<KLogger>()
            every { log.info(any<() -> Any?>()) } returns Unit
            every { HttpAccessLogFilter.log } returns log

            val httpHandler = HttpAccessLogFilter().then(
                routes(
                    "/ok" GET { Response(Status.OK) },
                    "/error" GET { Response(Status.BAD_REQUEST) }
                )
            )

            // Check that we don't log HTTP 200
            httpHandler(Request(Method.GET, "/ok"))
            verify(exactly = 0) { log.info(any<() -> Any?>()) }

            // Check that we log everything else
            httpHandler(Request(Method.GET, "/error"))
            verify { log.info(any<() -> Any?>()) }
        }
    }

    "custom statuses" {
        mockkObject(HttpAccessLogFilter) {
            val log = mockk<KLogger>()
            every { log.info(any<() -> Any?>()) } returns Unit
            every { HttpAccessLogFilter.log } returns log

            val httpHandler = HttpAccessLogFilter(Status.FORBIDDEN).then(
                routes(
                    "/error" GET { Response(Status.FORBIDDEN) },
                    "/ok" GET { Response(Status.OK) }
                )
            )

            // Check that we don't log HTTP FORBIDDEN
            httpHandler(Request(Method.GET, "/error"))
            verify(exactly = 0) { log.info(any<() -> Any?>()) }

            // Check that we log everything else
            httpHandler(Request(Method.GET, "/ok"))
            verify { log.info(any<() -> Any?>()) }
        }
    }
})
