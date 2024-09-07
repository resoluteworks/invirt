package invirt.http4k.views

import invirt.http4k.InvirtFilter
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes

class ErrorResponseTest : StringSpec({

    beforeSpec { initialiseInvirtViews() }

    "errorResponse - response status" {
        val errors = ValidationErrors(ValidationError("test", "test"))

        InvirtFilter()
            .then(routes("/test" bind Method.GET to { errorResponse("model", errors, "errorResponse-utils-test") }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.UNPROCESSABLE_ENTITY

        InvirtFilter()
            .then(routes("/test" bind Method.GET to { errorResponse("model", errors, "errorResponse-utils-test", Status.BAD_REQUEST) }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.BAD_REQUEST
    }

    "errorResponse - error pairs" {
        InvirtFilter()
            .then(routes("/test" bind Method.GET to { errorResponse("errorResponse-utils-test", "test" to uuid7()) }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.UNPROCESSABLE_ENTITY
    }

    "errorResponse - fail on empty errors" {
        shouldThrow<IllegalArgumentException> {
            errorResponse("test")
        }
    }
})
