package invirt.http4k

import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveBody
import org.http4k.routing.routes

class RequestScopeTest : StringSpec() {

    init {
        "value from request" {
            val handler = requestScopeValue(threadLocal) { it.uri.toString() }
                .then(
                    routes(
                        "/{value}" GET { Response(Status.OK).body(threadLocal.get()) }
                    )
                )
            handler(Request(Method.GET, "/test")) shouldHaveBody "/test"
            handler(Request(Method.GET, "/something-else")) shouldHaveBody "/something-else"
        }

        "value cleared after request even when error occurs" {
            val valueFromRequest = uuid7()
            lateinit var valueInRequestHandler: String
            val handler = requestScopeValue(threadLocal) { valueFromRequest }
                .then(
                    routes(
                        "/{value}" GET {
                            valueInRequestHandler = threadLocal.get()
                            throw IllegalStateException("Cannot proceed")
                        }
                    )
                )
            shouldThrowWithMessage<IllegalStateException>("Cannot proceed") {
                handler(Request(Method.GET, "/test"))
            }
            valueInRequestHandler shouldBe valueFromRequest
            threadLocal.get() shouldBe null
        }
    }

    companion object {
        val threadLocal = ThreadLocal<String>()
    }
}
