package invirt.test

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtConfig
import invirt.core.InvirtPebbleConfig
import invirt.core.views.ViewResponse
import invirt.core.views.errorResponse
import invirt.core.views.ok
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.validk.ValidationError
import io.validk.ValidationErrors
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.routes

class ResponseAssertionsTest : StringSpec() {

    private val invirt = Invirt(InvirtConfig(pebble = InvirtPebbleConfig(classpathLocation = "views")))

    init {

        "shouldHaveViewModel extracts and checks the ViewModel" {
            data class TestViewModel(val name: String) : ViewResponse("test-view")

            val httpHandler = invirt.then(routes("/test" GET { TestViewModel("test name").ok() }))
            val response = httpHandler(Request(Method.GET, "/test"))
            val viewModel = response.shouldHaveViewModel<TestViewModel>()
            viewModel.name shouldBe "test name"
        }

        "shouldBeErrorResponse extracts and inner view model and validation errors" {
            data class TestViewModel(val name: String) : ViewResponse("test-view")

            val httpHandler = invirt.then(
                routes(
                    "/test" GET {
                        errorResponse(TestViewModel("test name"), ValidationErrors(ValidationError("name", "Name too short")), "test-view")
                    }
                )
            )
            val response = httpHandler(Request(Method.GET, "/test"))
            response.status shouldBe Status.UNPROCESSABLE_ENTITY
            val (viewModel, errors) = response.shouldBeErrorResponse<TestViewModel>()
            viewModel.name shouldBe "test name"
            errors shouldBe ValidationErrors(ValidationError("name", "Name too short"))
        }

        "shouldBeErrorResponse without model" {
            val httpHandler = invirt.then(
                routes(
                    "/test" GET {
                        errorResponse(null, ValidationErrors(ValidationError("name", "Name too long")), "test-view")
                    }
                )
            )
            val response = httpHandler(Request(Method.GET, "/test"))
            response.status shouldBe Status.UNPROCESSABLE_ENTITY
            val errors = response.shouldBeErrorResponse()
            errors shouldBe ValidationErrors(ValidationError("name", "Name too long"))
        }
    }
}
