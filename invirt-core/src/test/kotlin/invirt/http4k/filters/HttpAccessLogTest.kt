package invirt.http4k.filters

import invirt.utils.uuid7
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.asRouter
import org.http4k.routing.bind

class HttpAccessLogTest : StringSpec() {

    init {

        "defaults only log errors" {
            val filter = HttpAccessLog()
            loggerShouldNotBeCalled(filter, Request(Method.GET, "/test"), Status.OK)
            loggerShouldNotBeCalled(filter, Request(Method.GET, "/test"), Status.SEE_OTHER)

            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.BAD_REQUEST)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.FORBIDDEN)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.UNAUTHORIZED)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.INTERNAL_SERVER_ERROR)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.BAD_GATEWAY)
        }

        "all statuses are logged when allStatues = true" {
            val filter = HttpAccessLog(allStatues = true)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.OK)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.SEE_OTHER)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.BAD_REQUEST)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.FORBIDDEN)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.UNAUTHORIZED)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.INTERNAL_SERVER_ERROR)
            loggerShouldBeCalled(filter, Request(Method.GET, "/test"), Status.BAD_GATEWAY)
        }

        "ignorePaths prevents logging even when allStatues = true" {
            loggerShouldNotBeCalled(
                HttpAccessLog(allStatues = true, ignorePaths = setOf("/status/200")),
                Request(Method.GET, "/status/200"),
                Status.OK
            )

            loggerShouldNotBeCalled(
                HttpAccessLog(allStatues = true, ignorePaths = setOf("/status/404")),
                Request(Method.GET, "/status/404"),
                Status.BAD_REQUEST
            )
        }

        "ignorePaths prevents doesn't prevent logging when the path doesn't match" {
            loggerShouldBeCalled(
                HttpAccessLog(allStatues = true, ignorePaths = setOf("/something-else")),
                Request(Method.GET, "/test"),
                Status.OK
            )

            loggerShouldBeCalled(
                HttpAccessLog(allStatues = true, ignorePaths = setOf("/something-else")),
                Request(Method.GET, "/test"),
                Status.BAD_REQUEST
            )
        }

        "excludeHeaders" {
            loggerShouldBeCalled(
                HttpAccessLog(
                    allStatues = true,
                    excludeHeaders = setOf("x-custom-header")
                ),
                Request(Method.DELETE, "/test").header("x-custom-header", "custom-header")
            ) { builder ->
                (builder.payload!!["headers"] as Map<String, List<String?>>)["x-custom-header"] shouldBe null
            }
        }

        "extra fields" {
            val clientId = uuid7()
            loggerShouldBeCalled(
                HttpAccessLog(
                    allStatues = true,
                    extraFields = { tx -> mapOf("clientId" to tx.request.header("x-client-id")!!) }
                ),
                Request(Method.DELETE, "/test").header("x-client-id", clientId)
            ) { builder ->
                builder.payload!!["clientId"] shouldBe clientId
            }
        }
    }

    private fun loggerShouldBeCalled(
        loggingFilter: Filter,
        request: Request,
        responseStatus: Status = Status.OK,
        expect: (KLoggingEventBuilder) -> Unit = {}
    ) {
        mockkObject(HttpAccessLog) {
            val builderSlot = slot<KLoggingEventBuilder.() -> Unit>()
            val mockkLogger = mockk<KLogger>()
            every { mockkLogger.atInfo(capture(builderSlot)) } returns Unit

            every { HttpAccessLog.logger } answers {
                mockkLogger
            }
            val httpHandler = loggingFilter.then(
                { r: Request -> true }.asRouter() bind { Response(responseStatus) }
            )

            val response = httpHandler(request)
            response.status shouldBe responseStatus // Just to check our wiring here works alright

            verify { mockkLogger.atInfo(any<KLoggingEventBuilder.() -> Unit>()) }

            val builder = KLoggingEventBuilder()
            builderSlot.captured.invoke(builder)
            builder.message shouldBe "http-access"
            expect(builder)
        }
    }

    private fun loggerShouldNotBeCalled(
        loggingFilter: Filter,
        request: Request,
        responseStatus: Status = Status.OK
    ) {
        mockkObject(HttpAccessLog) {
            val mockLogger = mockk<KLogger>(relaxed = true)
            every { HttpAccessLog.logger } answers { mockLogger }
            val httpHandler = loggingFilter.then(
                { r: Request -> true }.asRouter() bind { Response(responseStatus) }
            )

            httpHandler(request)
            verify(exactly = 0) { mockLogger.atInfo(any<KLoggingEventBuilder.() -> Unit>()) }
        }
    }
}
