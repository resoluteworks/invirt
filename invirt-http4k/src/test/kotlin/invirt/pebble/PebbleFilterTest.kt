package invirt.pebble

import invirt.http4k.GET
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class PebbleFilterTest : StringSpec({

    "request thread local" {
        lateinit var requestFromHandler: Request
        val handler = invirtPebbleFilter.then(
            routes(
                "/test" GET { request ->
                    requestFromHandler = currentHttp4kRequest!!
                    Response(Status.OK)
                }
            )
        )
        val request = Request(Method.GET, "/test")
        handler(request) shouldHaveStatus Status.OK
        requestFromHandler shouldBe request
        currentHttp4kRequest shouldBe null
    }

    "request thread local cleared after request even when error occurs" {
        val handler = invirtPebbleFilter
            .then(routes("/{value}" GET { throw IllegalStateException("Cannot proceed") }))
        shouldThrowWithMessage<IllegalStateException>("Cannot proceed") {
            handler(Request(Method.GET, "/test"))
        }
        currentHttp4kRequest shouldBe null
    }
})
