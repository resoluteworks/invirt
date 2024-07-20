package invirt.pebble

import invirt.http4k.InvirtFilter
import invirt.http4k.views.initialiseInvirtViews
import invirt.http4k.views.ok
import invirt.http4k.views.renderTemplate
import invirt.http4k.views.withView
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
import java.time.LocalDateTime
import java.time.ZoneOffset

class PebbleFunctionsTest : StringSpec() {

    init {
        beforeSpec { initialiseInvirtViews() }

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

        "dateWithDaySuffix - LocalDate" {
            testFunctionModel("dateWithDaySuffix-LocalDate", "/test", mapOf("date" to LocalDate.of(2024, 5, 17)), "17th May 2024")
            testFunctionModel("dateWithDaySuffix-LocalDate", "/test", mapOf("date" to LocalDate.of(1905, 12, 2)), "2nd Dec 1905")
        }

        "dateWithDaySuffix - LocalDateTime" {
            testFunctionModel(
                "dateWithDaySuffix-LocalDateTime", "/test",
                mapOf(
                    "date" to LocalDateTime.ofInstant(LocalDateTime.of(2024, 5, 17, 23, 10, 43).toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
                ),
                "17th May 2024 23:10:43"
            )
        }

        "dateWithDaySuffix - Instant" {
            testFunctionModel(
                "dateWithDaySuffix-Instant", "/test",
                mapOf(
                    "date" to LocalDateTime.of(2024, 5, 17, 23, 10, 43).toInstant(ZoneOffset.UTC)
                ),
                "17th May 2024 23:10:43"
            )
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
            val httpHandler = InvirtFilter().then(routes("/test" bind Method.GET to { renderTemplate("function-uuid") }))
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
        val httpHandler = InvirtFilter().then(routes("/test" bind Method.GET to { renderTemplate("function-${function}") }))
        val response = httpHandler(Request(Method.GET, request))
        response.bodyString().trim() shouldBe expectedBody
    }

    private fun testFunctionModel(function: String, request: String = "/test", model: Any, expectedBody: String) {
        val httpHandler = InvirtFilter().then(
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
