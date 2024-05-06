package invirt.pebble

import invirt.data.Page
import invirt.http4k.StoreRequestOnThread
import invirt.http4k.views.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import java.time.LocalDate

class PebbleFunctionsTest : StringSpec() {

    init {
        beforeAny { setDefaultViewLens(Views.Classpath("webapp/views")) }

        "today" {
            testFunction("today", "/test", "${LocalDate.now()}")
        }

        "request" {
            testFunction(
                "request",
                "/test?q=john",
                """
                   GET
                   /test?q=john
                   john
                """.trimIndent()
            )
        }

        "replaceQuery" {
            testFunction("replaceQuery", "/test", "/test?from=10")
            testFunction("replaceQuery", "/test?from=5", "/test?from=10")
            testFunction("replaceQuery", "/test?size=100&from=0", "/test?size=100&from=10")
        }

        "removeQueryValue" {
            testFunction("removeQueryValue", "/test", "/test")
            testFunction("removeQueryValue", "/test?filter=individual", "/test")
            testFunction("removeQueryValue", "/test?q=nothing&filter=entity&size=1", "/test?q=nothing&filter=entity&size=1")
            testFunction("removeQueryValue", "/test?q=nothing&filter=individual&size=1", "/test?q=nothing&size=1")
            testFunction(
                "removeQueryValue",
                "/test?q=nothing&filter=individual&size=1&filter=entity",
                "/test?q=nothing&size=1&filter=entity"
            )
        }

        "removeQueries" {
            testFunction("removeQueries", "/test", "/test")
            testFunction("removeQueries", "/test?filter=individual", "/test")
            testFunction("removeQueries", "/test?filter=nothing&filter=entity&size=1", "/test")
            testFunction("removeQueries", "/test?filter=nothing&q=john&filter=entity&size=1", "/test?q=john")
        }

        "replacePage" {
            val model = mapOf("page" to Page(30, 15))
            testFunctionModel("replacePage", "/test", model, "/test?from=30&size=15")
            testFunctionModel("replacePage", "/test?from=0&filter=individual&size=10", model, "/test?filter=individual&from=30&size=15")
            testFunctionModel("replacePage", "/test?from=0&filter=individual", model, "/test?filter=individual&from=30&size=15")
        }

        "toggleQueryValue" {
            testFunction("toggleQueryValue", "/test", "/test?filter=individual")
            testFunction("toggleQueryValue", "/test?filter=individual", "/test")
            testFunction(
                "toggleQueryValue",
                "/test?q=nothing&filter=entity&size=1",
                "/test?q=nothing&filter=entity&size=1&filter=individual"
            )
            testFunction(
                "toggleQueryValue",
                "/test?q=nothing&filter=entity&size=1&filter=individual",
                "/test?q=nothing&filter=entity&size=1"
            )
        }

        "dateWithDaySuffix" {
            testFunctionModel("dateWithDaySuffix", "/test", mapOf("date" to LocalDate.of(2024, 5, 17)), "17th May 2024")
            testFunctionModel("dateWithDaySuffix", "/test", mapOf("date" to LocalDate.of(1905, 12, 2)), "2nd Dec 1905")
        }

        "json" {
            data class Data(val name: String, val age: Int)

            val data = Data("John Smith", 27)
            testFunctionModel("json", "/test", mapOf("data" to data), """{"name":"John Smith","age":27}""")
        }

        "jsonArray" {
            data class Data(val name: String, val age: Int)

            val data = listOf(
                Data("John Smith", 27),
                Data("Jane Doe", 32)
            )
            testFunctionModel(
                "jsonArray",
                "/test",
                mapOf("data" to data),
                """[{"name":"John Smith","age":27},{"name":"Jane Doe","age":32}]"""
            )
        }

        "jsonArray from object" {
            data class Data(val name: String, val age: Int)

            val data = Data("John Smith", 27)
            testFunctionModel(
                "jsonArray-object",
                "/test",
                mapOf("data" to data),
                """[{"name":"John Smith","age":27}]"""
            )
        }

        "uuid" {
            val httpHandler = StoreRequestOnThread().then(routes("/test" bind Method.GET to { renderTemplate("function-uuid") }))
            val response1 = httpHandler(Request(Method.GET, "/test"))
            val response2 = httpHandler(Request(Method.GET, "/test"))

            response1.bodyString() shouldMatch "[0-9a-f]{32}"
            response2.bodyString() shouldMatch "[0-9a-f]{32}"
            response2.bodyString() shouldBeGreaterThan response1.bodyString()
        }

        "currencyFromMinorUnit" {
            testFunctionModel("currencyFromMinorUnit", "/test", mapOf("amount" to 10000, "currency" to "GBP"), "£100.00")
            testFunctionModel("currencyFromMinorUnit", "/test", mapOf("amount" to 2356, "currency" to "EUR"), "€23.56")
            testFunctionModel("currencyFromMinorUnit", "/test", mapOf("amount" to 603405, "currency" to "USD"), "$6,034.05")
        }
    }

    private fun testFunction(function: String, request: String, expectedBody: String) {
        val httpHandler = StoreRequestOnThread().then(routes("/test" bind Method.GET to { renderTemplate("function-${function}") }))
        val response = httpHandler(Request(Method.GET, request))
        response.bodyString().trim() shouldBe expectedBody
    }

    private fun testFunctionModel(function: String, request: String = "/test", model: Any, expectedBody: String) {
        val httpHandler = StoreRequestOnThread().then(
            routes(
                "/test" bind Method.GET to {
                    if (model is ViewModel) {
                        model.ok()
                    } else if (model is Map<*, *>) {
                        (model as Map<String, Any>) withView "function-${function}"
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
