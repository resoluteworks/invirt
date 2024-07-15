package invirt.pebble

import invirt.http4k.GET
import invirt.http4k.InvirtRequestContext
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class PebbleFilterTest : StringSpec({

    "request thread local" {
        lateinit var requestFromHandler: Request
        val handler = InvirtRequestContext().then(
            routes(
                "/test" GET { request ->
                    requestFromHandler = InvirtRequestContext.currentRequest!!
                    Response(Status.OK)
                }
            )
        )
        val request = Request(Method.GET, "/test")
        handler(request) shouldHaveStatus Status.OK
        requestFromHandler.uri shouldBe request.uri
        requestFromHandler.method shouldBe request.method
        InvirtRequestContext.currentRequest shouldBe null
    }

    "request thread local cleared after request even when error occurs" {
        val handler = InvirtRequestContext()
            .then(routes("/{value}" GET { throw IllegalStateException("Cannot proceed") }))
        shouldThrowWithMessage<IllegalStateException>("Cannot proceed") {
            handler(Request(Method.GET, "/test"))
        }
        InvirtRequestContext.currentRequest shouldBe null
    }
})
