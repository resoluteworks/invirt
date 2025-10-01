package invirt.core.views

import invirt.core.Invirt
import invirt.utils.cleanWhitespace
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
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.routing.routes

class ErrorResponseTest : StringSpec({

    "errorResponse - response status" {
        val errors = ValidationErrors(ValidationError("test", "test"))

        Invirt()
            .then(routes("/test" bind Method.GET to { errorResponse(it, errors, "errorResponse-utils-test") }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.UNPROCESSABLE_ENTITY

        Invirt()
            .then(routes("/test" bind Method.GET to { errorResponse(it, errors, "errorResponse-utils-test", null, Status.BAD_REQUEST) }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.BAD_REQUEST
    }

    "errorResponse - error pairs" {
        Invirt()
            .then(routes("/test" bind Method.GET to { errorResponse(it, "errorResponse-utils-test", "test" to uuid7()) }))
            .invoke(Request(Method.GET, "/test"))
            .status shouldBe Status.UNPROCESSABLE_ENTITY
    }

    "ViewResponse.toErrorResponse" {
        data class TestViewResponse(val name: String) : InvirtView("errorResponse-view-response-to-error-response")

        val response = Invirt()
            .then(
                routes(
                    "/test" bind Method.GET to {
                        TestViewResponse("an-incorrect-name").asErrorResponse(it, ValidationErrors(ValidationError("name", "Invalid name")))
                    }
                )
            )
            .invoke(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.UNPROCESSABLE_ENTITY
        response.bodyString().cleanWhitespace() shouldBe "an-incorrect-name Invalid name"
    }

    "errorResponse - fail on empty errors" {
        shouldThrow<IllegalArgumentException> {
            errorResponse(Request(Method.GET, "/test"), "test")
        }
    }
})
