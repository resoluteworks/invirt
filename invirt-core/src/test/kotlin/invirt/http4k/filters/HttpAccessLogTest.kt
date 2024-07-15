package invirt.http4k.filters

import invirt.http4k.GET
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.path
import org.http4k.routing.routes

class HttpAccessLogTest : StringSpec() {

    init {

        "defaults - errors only" {
            val filter = HttpAccessLog()
            testLogFilter(filter, Status.OK, false)
            testLogFilter(filter, Status.SEE_OTHER, false)
            testLogFilter(filter, Status.BAD_REQUEST, true)
            testLogFilter(filter, Status.FORBIDDEN, true)
            testLogFilter(filter, Status.UNAUTHORIZED, true)
            testLogFilter(filter, Status.INTERNAL_SERVER_ERROR, true)
            testLogFilter(filter, Status.BAD_GATEWAY, true)
        }

        "all statuses" {
            val filter = HttpAccessLog(true)
            testLogFilter(filter, Status.OK, true)
            testLogFilter(filter, Status.SEE_OTHER, true)
            testLogFilter(filter, Status.BAD_REQUEST, true)
            testLogFilter(filter, Status.FORBIDDEN, true)
            testLogFilter(filter, Status.UNAUTHORIZED, true)
            testLogFilter(filter, Status.INTERNAL_SERVER_ERROR, true)
            testLogFilter(filter, Status.BAD_GATEWAY, true)
        }

        "ignorePaths" {
            testLogFilter(HttpAccessLog(true, setOf("/status/200")), Status.OK, false)
            testLogFilter(HttpAccessLog(false, setOf("/status/200")), Status.OK, false)
            testLogFilter(HttpAccessLog(true, setOf("/status/404")), Status.NOT_FOUND, false)
            testLogFilter(HttpAccessLog(false, setOf("/status/404")), Status.NOT_FOUND, false)
            testLogFilter(HttpAccessLog(false, setOf("/status/400")), Status.NOT_FOUND, true)

            testLogFilter(HttpAccessLog(true, setOf("/status")), Status.NOT_FOUND, false)
            testLogFilter(HttpAccessLog(true, setOf("/status")), Status.BAD_REQUEST, false)
            testLogFilter(HttpAccessLog(true, setOf("/status")), Status.OK, false)
        }
    }

    private fun testLogFilter(filter: Filter, status: Status, expectCalled: Boolean) {
        mockkObject(HttpAccessLog) {
            every { HttpAccessLog.logHttpTransaction(any(), any(), any()) } returns Unit

            val httpHandler = filter.then(
                routes(
                    "/status/{status}" GET { Response(Status(it.path("status")!!.toInt(), "")) }
                )
            )
            httpHandler(Request(Method.GET, "/status/${status.code}"))
            if (expectCalled) {
                verify { HttpAccessLog.logHttpTransaction(any(), any(), any()) }
            } else {
                verify(exactly = 0) { HttpAccessLog.logHttpTransaction(any(), any(), any()) }
            }
        }
    }
}
