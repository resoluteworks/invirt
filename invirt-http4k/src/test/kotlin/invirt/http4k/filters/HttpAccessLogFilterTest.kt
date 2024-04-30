package invirt.http4k.filters

import invirt.http4k.GET
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.http4k.core.*
import org.http4k.routing.path
import org.http4k.routing.routes

class HttpAccessLogFilterTest : StringSpec() {

    init {

        "defaults - errors only" {
            val filter = HttpAccessLogFilter()
            testLogFilter(filter, Status.OK, false)
            testLogFilter(filter, Status.SEE_OTHER, false)
            testLogFilter(filter, Status.BAD_REQUEST, true)
            testLogFilter(filter, Status.FORBIDDEN, true)
            testLogFilter(filter, Status.UNAUTHORIZED, true)
            testLogFilter(filter, Status.INTERNAL_SERVER_ERROR, true)
            testLogFilter(filter, Status.BAD_GATEWAY, true)
        }

        "all statuses" {
            val filter = HttpAccessLogFilter(false)
            testLogFilter(filter, Status.OK, true)
            testLogFilter(filter, Status.SEE_OTHER, true)
            testLogFilter(filter, Status.BAD_REQUEST, true)
            testLogFilter(filter, Status.FORBIDDEN, true)
            testLogFilter(filter, Status.UNAUTHORIZED, true)
            testLogFilter(filter, Status.INTERNAL_SERVER_ERROR, true)
            testLogFilter(filter, Status.BAD_GATEWAY, true)
        }
    }

    private fun testLogFilter(filter: Filter, status: Status, expectCalled: Boolean) {
        mockkObject(HttpAccessLogFilter) {
            val log = mockk<KLogger>()
            every { log.atInfo(any<(KLoggingEventBuilder) -> Unit>()) } returns Unit
            every { HttpAccessLogFilter.log } returns log

            val httpHandler = filter.then(
                routes(
                    "/status/{status}" GET { Response(Status(it.path("status")!!.toInt(), "")) }
                )
            )
            httpHandler(Request(Method.GET, "/status/${status.code}"))
            if (expectCalled) {
                verify { log.atInfo(any<(KLoggingEventBuilder) -> Unit>()) }
            } else {
                verify(exactly = 0) { log.atInfo(any<(KLoggingEventBuilder) -> Unit>()) }
            }
        }
    }
}
