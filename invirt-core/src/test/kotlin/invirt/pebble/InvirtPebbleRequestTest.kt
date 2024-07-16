package invirt.pebble

import invirt.data.Page
import invirt.http4k.InvirtFilter
import invirt.http4k.views.initialiseInvirtViews
import invirt.http4k.views.ok
import invirt.http4k.views.renderTemplate
import invirt.http4k.views.withView
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.ViewModel

class InvirtPebbleRequestTest : StringSpec() {

    init {
        beforeSpec { initialiseInvirtViews() }

        "replaceQuery" {
            testRequestFunction("replaceQuery", "/test", "/test?from=10")
            testRequestFunction("replaceQuery", "/test?from=5", "/test?from=10")
            testRequestFunction("replaceQuery", "/test?size=100&from=0", "/test?size=100&from=10")
        }

        "removeQueryValue" {
            testRequestFunction("removeQueryValue", "/test", "/test")
            testRequestFunction("removeQueryValue", "/test?filter=individual", "/test")
            testRequestFunction("removeQueryValue", "/test?q=nothing&filter=entity&size=1", "/test?q=nothing&filter=entity&size=1")
            testRequestFunction("removeQueryValue", "/test?q=nothing&filter=individual&size=1", "/test?q=nothing&size=1")
            testRequestFunction(
                "removeQueryValue",
                "/test?q=nothing&filter=individual&size=1&filter=entity",
                "/test?q=nothing&size=1&filter=entity"
            )
        }

        "removeQueries" {
            testRequestFunction("removeQueries", "/test", "/test")
            testRequestFunction("removeQueries", "/test?filter=individual", "/test")
            testRequestFunction("removeQueries", "/test?filter=nothing&filter=entity&size=1", "/test")
            testRequestFunction("removeQueries", "/test?filter=nothing&q=john&filter=entity&size=1", "/test?q=john")
        }

        "replacePage" {
            val model = mapOf("page" to Page(30, 15))
            testRequestFunctionModel("replacePage", "/test", model, "/test?from=30&size=15")
            testRequestFunctionModel(
                "replacePage",
                "/test?from=0&filter=individual&size=10",
                model,
                "/test?filter=individual&from=30&size=15"
            )
            testRequestFunctionModel(
                "replacePage", "/test?from=0&filter=individual",
                model, "/test?filter=individual&from=30&size=15"
            )
        }

        "toggleQueryValue" {
            testRequestFunction("toggleQueryValue", "/test", "/test?filter=individual")
            testRequestFunction("toggleQueryValue", "/test?filter=individual", "/test")
            testRequestFunction(
                "toggleQueryValue",
                "/test?q=nothing&filter=entity&size=1",
                "/test?q=nothing&filter=entity&size=1&filter=individual"
            )
            testRequestFunction(
                "toggleQueryValue",
                "/test?q=nothing&filter=entity&size=1&filter=individual",
                "/test?q=nothing&filter=entity&size=1"
            )
        }

        "hasQueryValue" {
            testRequestFunction("hasQueryValue", "/test", "false")
            testRequestFunction("hasQueryValue", "/test?type=person", "true")
            testRequestFunction("hasQueryValue", "/test?type=company&size=10&type=person", "true")
            testRequestFunction("hasQueryValue", "/test?type=company&size=10&type=entity", "false")
        }
    }

    private fun testRequestFunction(function: String, request: String, expectedBody: String) {
        val httpHandler = InvirtFilter().then(routes("/test" bind Method.GET to { renderTemplate("request-function-${function}") }))
        val response = httpHandler(Request(Method.GET, request))
        response.bodyString().trim() shouldBe expectedBody
    }

    private fun testRequestFunctionModel(function: String, request: String = "/test", model: Any, expectedBody: String) {
        val httpHandler = InvirtFilter().then(
            routes(
                "/test" bind Method.GET to {
                    if (model is ViewModel) {
                        model.ok()
                    } else if (model is Map<*, *>) {
                        (model as Map<String, Any>) withView "request-function-${function}"
                    } else {
                        throw IllegalArgumentException("Can't handle model $model")
                    }
                }
            )
        )
        val response = httpHandler(Request(Method.GET, request))
        response.bodyString().trim() shouldBe expectedBody
    }
}
