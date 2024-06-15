package invirt.pebble

import invirt.http4k.AppRequestContexts
import invirt.http4k.StoreRequestOnThread
import invirt.http4k.views.Views
import invirt.http4k.views.errorResponse
import invirt.http4k.views.setDefaultViewLens
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
            class Form

            val errors = ValidationErrors(
                ValidationError("name", "Name too short"),
                ValidationError("email", "Not a valid email"),
                ValidationError("details.age", "Age must be 18 or over")
            )
            val httpHandler = AppRequestContexts().then(StoreRequestOnThread())
                .then(routes("/test" bind Method.GET to { Form().errorResponse(errors, "validation/errors-from-context") }))
            val response = httpHandler(Request(Method.GET, "/test"))
            response.bodyString().trimIndent() shouldBe """
                name - Name too short
                email - Not a valid email
                details.age - Age must be 18 or over
            """.trimIndent()
        }

        "errors.hasErrors" {
            class Form

            fun testErrors(errors: ValidationErrors, expect: Boolean) {
                val httpHandler = AppRequestContexts().then(StoreRequestOnThread())
                    .then(routes("/test" bind Method.GET to { Form().errorResponse(errors, "validation/has-errors") }))
                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect.toString()
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), true)
            testErrors(ValidationErrors(ValidationError("description", "Name too short")), false)
        }

        "errors function in macro" {
            class Form

            fun testErrors(errors: ValidationErrors, expect: String) {
                val httpHandler = AppRequestContexts().then(StoreRequestOnThread())
                    .then(routes("/test" bind Method.GET to { Form().errorResponse(errors, "validation/errors-function-in-macro.peb") }))
                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), "true - Name too short")
        }
    }
}
