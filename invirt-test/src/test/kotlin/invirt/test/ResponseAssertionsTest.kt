package invirt.test

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtPebbleConfig
import invirt.core.views.InvirtView
import invirt.core.views.asErrorResponse
import invirt.core.views.errorResponse
import invirt.core.views.ok
import invirt.core.views.renderTemplate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.routes

class ResponseAssertionsTest : StringSpec() {

    init {

        beforeAny {
            Invirt.configure(pebble = InvirtPebbleConfig(classpathLocation = "views"))
        }

        "shouldHaveModel extracts and checks the model" {
            data class TestViewModel(val name: String) : InvirtView("test-view")

            val httpHandler = routes("/test" GET { TestViewModel("test name").ok(it) })
            val response = httpHandler(Request(Method.GET, "/test"))
            val viewModel = response.shouldHaveModel<TestViewModel>()
            viewModel.name shouldBe "test name"
        }

        "shouldHaveTemplate extracts and checks the template" {
            val httpHandler = routes("/test" GET { renderTemplate(it, "test-view") })
            val response = httpHandler(Request(Method.GET, "/test"))
            response shouldHaveTemplate "test-view"
        }

        "shouldBeErrorResponse extracts and inner view model and validation errors" {
            data class TestViewModel(val name: String) : InvirtView("test-view")

            val httpHandler = routes(
                "/test" GET {
                    TestViewModel("test name").asErrorResponse(it, ValidationErrors(ValidationError("name", "Name too short")))
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.status shouldBe Status.UNPROCESSABLE_ENTITY
            val (viewModel, errors) = response.shouldBeErrorResponse<TestViewModel>()
            viewModel.name shouldBe "test name"
            errors shouldBe ValidationErrors(ValidationError("name", "Name too short"))
        }

        "shouldBeErrorResponse without model" {
            val httpHandler = routes(
                "/test" GET {
                    errorResponse(it, ValidationErrors(ValidationError("name", "Name too long")), "test-view")
                }
            )

            val response = httpHandler(Request(Method.GET, "/test"))
            response.status shouldBe Status.UNPROCESSABLE_ENTITY
            val errors = response.shouldBeErrorResponse()
            errors shouldBe ValidationErrors(ValidationError("name", "Name too long"))
        }
    }
}
