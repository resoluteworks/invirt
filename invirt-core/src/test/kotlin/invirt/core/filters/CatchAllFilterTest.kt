package invirt.core.filters

import invirt.core.GET
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KLoggingEventBuilder
import io.github.oshai.kotlinlogging.Level
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class CatchAllFilterTest : StringSpec({

    "exception to status" {
        val handler = CatchAll(
            IllegalArgumentException::class to Status.BAD_REQUEST,
            ClassCastException::class to Status.NOT_FOUND
        ).then(
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

    "a response is passed through and nothing is logged" {
        val handler = CatchAll(IllegalArgumentException::class to Status.BAD_REQUEST)
            .then(routes("/created" GET { Response(Status.CREATED).body("Created") }))

        val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/created"))

        response shouldHaveStatus Status.CREATED
        response.bodyString() shouldBe "Created"
        events.shouldBeEmpty()
    }

    "an exception mapped to a 4xx status is logged at WARN" {
        val exception = IllegalArgumentException("Not a valid name")
        val handler = CatchAll(IllegalArgumentException::class to Status.BAD_REQUEST)
            .then(routes("/illegal-argument" GET { throw exception }))

        val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/illegal-argument"))

        response shouldHaveStatus Status.BAD_REQUEST
        events.shouldHaveLoggedOnly(Level.WARN, status = 400, errorMessage = "Not a valid name", cause = exception)
    }

    "an unmapped exception answers 500 and is logged at ERROR" {
        val exception = RuntimeException("Connection reset")
        val handler = CatchAll(IllegalArgumentException::class to Status.BAD_REQUEST)
            .then(routes("/internal" GET { throw exception }))

        val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/internal"))

        response shouldHaveStatus Status.INTERNAL_SERVER_ERROR
        events.shouldHaveLoggedOnly(Level.ERROR, status = 500, errorMessage = "Connection reset", cause = exception)
    }

    "an exception mapped to a 5xx status is logged at ERROR" {
        val exception = IllegalStateException("Downstream is unavailable")
        val handler = CatchAll(IllegalStateException::class to Status.SERVICE_UNAVAILABLE)
            .then(routes("/unavailable" GET { throw exception }))

        val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/unavailable"))

        response shouldHaveStatus Status.SERVICE_UNAVAILABLE
        events.shouldHaveLoggedOnly(Level.ERROR, status = 503, errorMessage = "Downstream is unavailable", cause = exception)
    }

    "the level flips at status 500" {
        listOf(
            Status.FOUND to Level.WARN,
            Status.NOT_FOUND to Level.WARN,
            Status(499, "Client Closed Request") to Level.WARN,
            Status.INTERNAL_SERVER_ERROR to Level.ERROR,
            Status.SERVICE_UNAVAILABLE to Level.ERROR,
            Status(599, "Network Connect Timeout Error") to Level.ERROR
        ).forEach { (status, level) ->
            withClue("A status of $status") {
                val handler = CatchAll(IllegalStateException::class to status)
                    .then(routes("/boom" GET { throw IllegalStateException("Boom") }))

                val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/boom"))

                response shouldHaveStatus status
                events.map { it.level } shouldBe listOf(level)
            }
        }
    }

    "an exception without a message is logged with a null errorMessage" {
        val exception = IllegalStateException()
        val handler = CatchAll(IllegalArgumentException::class to Status.BAD_REQUEST)
            .then(routes("/no-message" GET { throw exception }))

        val (response, events) = handleAndCaptureLog(handler, Request(Method.GET, "/no-message"))

        response shouldHaveStatus Status.INTERNAL_SERVER_ERROR
        events.shouldHaveLoggedOnly(Level.ERROR, status = 500, errorMessage = null, cause = exception)
    }
})

/** The constant message [CatchAll] logs for every exception it catches. */
private const val CAUGHT_MESSAGE = "Request failed with an exception"

/** A log event as [CatchAll] emits it, read out of the [KLoggingEventBuilder] that carries it. */
private data class LoggedEvent(
    val level: Level,
    val message: String?,
    val payload: Map<String, Any?>?,
    val cause: Throwable?
)

/**
 * Asserts that exactly one event was logged: at [level], with the constant message, [status] and [errorMessage] as its
 * payload, and [cause] itself as its cause. The cause is compared by identity because Kotest's `shouldBe` treats two
 * exceptions of the same class and message as equal.
 */
private fun List<LoggedEvent>.shouldHaveLoggedOnly(level: Level, status: Int, errorMessage: String?, cause: Throwable) {
    this shouldBe listOf(
        LoggedEvent(level, CAUGHT_MESSAGE, mapOf("status" to status, "errorMessage" to errorMessage), cause)
    )
    single().cause shouldBeSameInstanceAs cause
}

/**
 * Sends [request] through [handler] while the logger of [CatchAll] is a mock, and returns the response together with
 * the events logged on the way.
 */
private fun handleAndCaptureLog(handler: HttpHandler, request: Request): Pair<Response, List<LoggedEvent>> {
    val events = mutableListOf<LoggedEvent>()
    lateinit var response: Response
    mockkObject(CatchAll) {
        val logger = mockk<KLogger>()
        every { logger.at(any(), any(), any()) } answers {
            val builder = KLoggingEventBuilder().apply(thirdArg<KLoggingEventBuilder.() -> Unit>())
            events += LoggedEvent(firstArg(), builder.message, builder.payload, builder.cause)
        }
        every { CatchAll.logger } returns logger
        response = handler(request)
    }
    return response to events
}
