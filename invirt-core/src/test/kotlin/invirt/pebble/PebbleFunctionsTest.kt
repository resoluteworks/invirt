package invirt.pebble

import invirt.core.Invirt
import invirt.core.views.InvirtView
import invirt.core.views.ok
import invirt.core.views.renderTemplate
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.pebbletemplates.pebble.error.PebbleException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class PebbleFunctionsTest : StringSpec() {

    init {
        "plural" {
            testFunction("plural", "/test", "dogs\ncat\ncars")
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

        "dateWithDaySuffix - LocalDate ignores a zone argument" {
            testFunctionModel(
                "dateWithDaySuffix-literal-zone", "/test",
                mapOf("date" to LocalDate.of(2026, 9, 1)),
                "1st September 2026"
            )
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

        "dateWithDaySuffix - Instant with an explicit zone" {
            testFunctionModel(
                "dateWithDaySuffix-Instant", "/test",
                mapOf(
                    "date" to LocalDateTime.of(2024, 5, 17, 23, 10, 43).toInstant(ZoneOffset.UTC),
                    "zone" to "UTC"
                ),
                "17th May 2024 23:10:43"
            )

            // 23:02 UTC is already the next day in London (BST), so the zone picks the day and its suffix
            val secondOfAugust = Instant.parse("2026-08-02T23:02:00Z")
            testFunctionModel(
                "dateWithDaySuffix-Instant", "/test",
                mapOf("date" to secondOfAugust, "zone" to "UTC"),
                "2nd Aug 2026 23:02:00"
            )
            testFunctionModel(
                "dateWithDaySuffix-Instant", "/test",
                mapOf("date" to secondOfAugust, "zone" to "Europe/London"),
                "3rd Aug 2026 00:02:00"
            )

            // and on the last night of August that also moves the month
            testFunctionModel(
                "dateWithDaySuffix-literal-zone", "/test",
                mapOf("date" to Instant.parse("2026-08-31T23:02:00Z")),
                "1st September 2026"
            )
        }

        "dateWithDaySuffix - LocalDateTime ignores a zone argument" {
            testFunctionModel(
                "dateWithDaySuffix-LocalDateTime-zone", "/test",
                mapOf("date" to LocalDateTime.of(2024, 5, 17, 23, 10, 43)),
                "17th May 2024 23:10:43"
            )
        }

        "dateWithDaySuffix - no format pattern" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel("dateWithDaySuffix-no-format", model = mapOf("date" to LocalDate.of(2026, 9, 1)))
            }
            exception.message shouldContain "Filter [dateWithDaySuffix] needs a format pattern"
        }

        "dateWithDaySuffix - Instant without a zone" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel("dateWithDaySuffix-no-zone", model = mapOf("date" to Instant.parse("2026-08-31T23:02:00Z")))
            }
            exception.message shouldContain "Filter [dateWithDaySuffix] needs an explicit zone to format an Instant"
            exception.message shouldContain "dateWithDaySuffix-no-zone"
        }

        "dateWithDaySuffix - invalid zone id" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel(
                    "dateWithDaySuffix-Instant",
                    model = mapOf("date" to Instant.parse("2026-08-31T23:02:00Z"), "zone" to "Europe/Nowhere")
                )
            }
            exception.message shouldContain "Filter [dateWithDaySuffix] was given an invalid zone id [Europe/Nowhere]"
        }

        "dateWithDaySuffix - value that isn't a date" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel("dateWithDaySuffix-no-zone", model = mapOf("date" to "2026-08-31"))
            }
            exception.message shouldContain "Filter [dateWithDaySuffix] can't format a value of type [java.lang.String]"
        }

        "dateWithDaySuffix - null value" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel("dateWithDaySuffix-no-zone", model = mapOf("date" to null))
            }
            exception.message shouldContain "Filter [dateWithDaySuffix] was given a null value"
        }

        "dateRange" {
            // The default separator, written from its code point so the source of the test stays ASCII.
            val enDash = Char(0x2013)

            testFunctionModel(
                "dateRange", "/test",
                mapOf("from" to LocalDate.of(2026, 11, 5), "to" to LocalDate.of(2026, 12, 5)),
                "5th November $enDash 5th December 2026"
            )

            // a null "to" is how a single-day entry is modelled, so it is not an error
            testFunctionModel(
                "dateRange", "/test",
                mapOf("from" to LocalDate.of(2026, 11, 5), "to" to null),
                "5th November 2026"
            )

            testFunctionModel(
                "dateRange", "/test",
                mapOf("from" to LocalDate.of(2026, 12, 30), "to" to LocalDate.of(2027, 1, 2)),
                "30th December 2026 $enDash 2nd January 2027"
            )

            // a range sharing both month and year says the month and year once, joined by an unspaced en dash
            testFunctionModel(
                "dateRange", "/test",
                mapOf("from" to LocalDate.of(2026, 12, 6), "to" to LocalDate.of(2026, 12, 10)),
                "6th${enDash}10th December 2026"
            )
        }

        "dateRange - a from that isn't a date" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel("dateRange", model = mapOf("from" to "2026-11-05", "to" to null))
            }
            exception.message shouldContain "Function [dateRange] needs a LocalDate [from], was [2026-11-05]"
            exception.message shouldContain "function-dateRange"
        }

        "dateRange - a to that isn't a date" {
            val exception = shouldThrow<PebbleException> {
                renderFunctionModel(
                    "dateRange",
                    model = mapOf("from" to LocalDate.of(2026, 11, 5), "to" to "2026-12-05")
                )
            }
            exception.message shouldContain "Function [dateRange] needs a LocalDate or null [to], was [2026-12-05]"
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
            Invirt.configure()
            val httpHandler = routes("/test" bind Method.GET to { renderTemplate(it, "function-uuid") })
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
        Invirt.configure()
        val httpHandler = routes("/test" bind Method.GET to { renderTemplate(it, "function-${function}") })
        val response = httpHandler(Request(Method.GET, request))
        response.bodyString().trim() shouldBe expectedBody
    }

    private fun testFunctionModel(function: String, request: String = "/test", model: Any, expectedBody: String) {
        renderFunctionModel(function, request, model) shouldBe expectedBody
    }

    private fun renderFunctionModel(function: String, request: String = "/test", model: Any): String {
        Invirt.configure()
        val httpHandler = routes(
            "/test" bind Method.GET to {
                when (model) {
                    is InvirtView -> model.ok(it)
                    is Map<*, *> -> renderTemplate(it, "function-${function}", model)
                    else -> throw IllegalArgumentException("Can't handle model $model")
                }
            }
        )
        return httpHandler(Request(Method.GET, request)).bodyString().trim()
    }
}
