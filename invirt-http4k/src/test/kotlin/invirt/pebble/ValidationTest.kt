package invirt.pebble

import invirt.http4k.AppRequestContexts
import invirt.http4k.ViewResponse
import invirt.http4k.Views
import invirt.http4k.setDefaultViewLens
import invirt.http4k.views.errorResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes

class ValidationTest : StringSpec() {

    init {
        beforeAny { setDefaultViewLens(Views.Classpath("webapp/views")) }

        "errors from context" {
            class Form : ViewResponse("validation/errors-from-context")

            val errors = ValidationErrors(
                ValidationError("name", "Name too short"),
                ValidationError("email", "Not a valid email"),
                ValidationError("details.age", "Age must be 18 or over"),
            )
            val httpHandler = AppRequestContexts().then(invirtPebbleFilter)
                .then(routes("/test" bind Method.GET to { Form().errorResponse(errors) }))
            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trimIndent() shouldBe """
                name - Name too short
                email - Not a valid email
                details.age - Age must be 18 or over
            """.trimIndent()
        }

        "errors.hasErrors" {
            class Form : ViewResponse("validation/has-errors")

            fun testErrors(errors: ValidationErrors, expect: Boolean) {
                val httpHandler = AppRequestContexts().then(invirtPebbleFilter)
                    .then(routes("/test" bind Method.GET to { Form().errorResponse(errors) }))
                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect.toString()
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), true)
            testErrors(ValidationErrors(ValidationError("description", "Name too short")), false)
        }

        "errors function in macro" {
            class Form : ViewResponse("validation/errors-function-in-macro.peb")

            fun testErrors(errors: ValidationErrors, expect: String) {
                val httpHandler = AppRequestContexts().then(invirtPebbleFilter)
                    .then(routes("/test" bind Method.GET to { Form().errorResponse(errors) }))
                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), "true - Name too short")
        }
    }
}
