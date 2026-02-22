package invirt.pebble

import invirt.core.Invirt
import invirt.core.views.errorResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.bind
import org.http4k.routing.routes

class ValidationTest : StringSpec() {

    init {

        beforeAny {
            Invirt.configure()
        }

        "errors from context" {
            class Form

            val errors = ValidationErrors(
                ValidationError("name", "Name too short"),
                ValidationError("email", "Not a valid email"),
                ValidationError("details.age", "Age must be 18 or over")
            )
            val httpHandler = routes(
                "/test" bind Method.GET to {
                    errorResponse(it, errors, "validation/errors-from-context", Form())
                }
            )

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
                val httpHandler = routes("/test" bind Method.GET to { errorResponse(it, errors, "validation/has-errors", Form()) })
                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect.toString()
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), true)
            testErrors(ValidationErrors(ValidationError("description", "Name too short")), false)
        }

        "errors function in macro" {
            class Form

            fun testErrors(errors: ValidationErrors, expect: String) {
                val httpHandler = routes(
                    "/test" bind Method.GET to {
                        errorResponse(
                            it,
                            errors,
                            "validation/errors-function-in-macro.peb",
                            Form()
                        )
                    }
                )

                val response = httpHandler(Request(Method.GET, "/test"))
                response.bodyString().trim() shouldBe expect
            }

            testErrors(ValidationErrors(ValidationError("name", "Name too short")), "true - Name too short")
        }
    }
}
