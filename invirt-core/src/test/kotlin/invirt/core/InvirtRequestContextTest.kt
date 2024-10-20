package invirt.core

import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.slot
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.LensFailure
import org.http4k.routing.routes

class InvirtRequestContextTest : StringSpec({

    "request thread local" {
        lateinit var requestFromHandler: Request
        val handler = Invirt().then(
            routes(
                "/test" GET { request ->
                    requestFromHandler = InvirtRequestContext.request!!
                    Response(Status.OK)
                }
            )
        )
        val request = Request(Method.GET, "/test")
        handler(request) shouldHaveStatus Status.OK
        requestFromHandler.uri shouldBe request.uri
        requestFromHandler.method shouldBe request.method
        InvirtRequestContext.request shouldBe null
    }

    "request thread local cleared after request even when error occurs" {
        val handler = Invirt()
            .then(routes("/{value}" GET { throw IllegalStateException("Cannot proceed") }))
        shouldThrowWithMessage<IllegalStateException>("Cannot proceed") {
            handler(Request(Method.GET, "/test"))
        }
        InvirtRequestContext.request shouldBe null
    }

    "optionalKey - default value" {
        val slot = slot<String>()

        val key = InvirtRequestContext.optionalKey<String>()

        val handler = Invirt().then { request ->
            slot.captured = key(request) ?: "some-default-value"
            Response(Status.OK)
        }

        // When no value is set, the default value should be used
        handler(Request(Method.GET, "/test"))
        slot.captured shouldBe "some-default-value"
    }

    "optionalKey - use value that was previously set" {
        val slot = slot<String>()

        val key = InvirtRequestContext.optionalKey<String>()
        val keyValue = uuid7()
        val handler = Invirt()
            .then(
                Filter { next ->
                    { request ->
                        key[request] = keyValue
                        next(request)
                    }
                }
            )
            .then { request ->
                slot.captured = key(request) ?: "some-default-value"
                Response(Status.OK)
            }

        // When a value is set, it should be used
        handler(Request(Method.GET, "/test"))
        slot.captured shouldBe keyValue
    }

    "requiredKey - use value that was previously set" {
        val slot = slot<String>()

        val key = InvirtRequestContext.requiredKey<String>()
        val keyValue = uuid7()
        val handler = Invirt()
            .then(
                Filter { next ->
                    { request ->
                        key[request] = keyValue
                        next(request)
                    }
                }
            )
            .then { request ->
                slot.captured = key(request)
                Response(Status.OK)
            }

        // When a value is set, it should be used
        handler(Request(Method.GET, "/test"))
        slot.captured shouldBe keyValue
    }

    "requiredKey - fail when no value is set" {
        val slot = slot<String>()

        val key = InvirtRequestContext.requiredKey<String>()
        val handler = Invirt()
            .then { request ->
                slot.captured = key(request)
                Response(Status.OK)
            }

        // When a value is not set an exception should be thrown
        shouldThrow<LensFailure> {
            handler(Request(Method.GET, "/test"))
        }
    }
})
