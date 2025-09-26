package invirt.test

import invirt.core.GET
import invirt.core.Invirt
import invirt.core.InvirtConfig
import invirt.core.InvirtPebbleConfig
import invirt.core.views.ViewResponse
import invirt.core.views.ok
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.routing.routes

class ResponseAssertionsTest : StringSpec({

    "shouldBeViewModel extracts and checks the ViewModel from InvirtResponse" {
        val config = InvirtConfig(pebble = InvirtPebbleConfig(classpathLocation = "views"))

        data class TestViewModel(val name: String) : ViewResponse("test-view")

        val httpHandler = Invirt(config).then(routes("/test" GET { TestViewModel("test name").ok() }))
        val response = httpHandler(Request(Method.GET, "/test"))
        val viewModel = response.shouldBeViewModel<TestViewModel>()
        viewModel.name shouldBe "test name"
    }
})
