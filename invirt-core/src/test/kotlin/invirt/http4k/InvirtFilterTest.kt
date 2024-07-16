package invirt.http4k

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

class InvirtFilterTest : StringSpec({

    "request thread local" {
        lateinit var requestFromHandler: Request
        val handler = InvirtFilter().then(
            routes(
                "/test" GET { request ->
                    requestFromHandler = InvirtFilter.currentRequest!!
                    Response(Status.OK)
                }
            )
        )
        val request = Request(Method.GET, "/test")
        handler(request) shouldHaveStatus Status.OK
        requestFromHandler.uri shouldBe request.uri
        requestFromHandler.method shouldBe request.method
        InvirtFilter.currentRequest shouldBe null
    }

    "request thread local cleared after request even when error occurs" {
        val handler = InvirtFilter()
            .then(routes("/{value}" GET { throw IllegalStateException("Cannot proceed") }))
        shouldThrowWithMessage<IllegalStateException>("Cannot proceed") {
            handler(Request(Method.GET, "/test"))
        }
        InvirtFilter.currentRequest shouldBe null
    }
})
