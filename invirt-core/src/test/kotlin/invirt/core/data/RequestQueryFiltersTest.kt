package invirt.core.data

import invirt.data.DataFilter
import invirt.data.DataFilter.Compound
import invirt.data.eq
import invirt.data.gt
import invirt.data.gte
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.lens.int
import java.time.LocalDate

class RequestQueryFiltersTest : StringSpec() {

    init {
        "query params to filter - OR" {
            val filter = queryValuesFilter {
                Query.multi.optional("type").or { "entity-type".eq(it) }
                Query.int().optional("min-age").filter { "minAge".gte(it) }
                Query.enum<MaritalStatus>().multi.optional("marital-status").or { "maritalStatus".eq(it) }
                Query.optional("signed-up").filter { value ->
                    when (value) {
                        "last-month" -> "signupDate".gte(LocalDate.now().minusDays(30))
                        "last-6-months" -> "signupDate".gte(LocalDate.now().minusDays(180))
                        else -> null
                    }
                }
                Query.multi.optional("subscription-type").and {
                    "subscriptionType" eq it
                }
            }

            filter(Request(Method.GET, "/test")) shouldBe null
            filter(Request(Method.GET, "/test?q=test")) shouldBe null

            filter(Request(Method.GET, "/test?type=person")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person")
            )

            filter(Request(Method.GET, "/test?type=person&type=company")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person", "entity-type" eq "company")
            )

            filter(Request(Method.GET, "/test?type=person&min-age=18")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"), "minAge".gte(18)
            )

            filter(Request(Method.GET, "/test?type=person&min-age=18&min-age=25")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"), "minAge".gte(18)
            )

            filter(
                Request(
                    Method.GET, "/test?type=person&min-age=18&marital-status=MARRIED&marital-status=SINGLE"
                )
            ) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"),
                "minAge".gte(18),
                Compound.or("maritalStatus" eq MaritalStatus.MARRIED, "maritalStatus" eq MaritalStatus.SINGLE)
            )

            filter(
                Request(
                    Method.GET, "/test?type=person&min-age=18&marital-status=MARRIED&marital-status=SINGLE&signed-up=last-month"
                )
            ) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"),
                "minAge".gte(18),
                Compound.or("maritalStatus" eq MaritalStatus.MARRIED, "maritalStatus" eq MaritalStatus.SINGLE),
                "signupDate".gte(LocalDate.now().minusDays(30))
            )

            filter(
                Request(
                    Method.GET,
                    "/test?type=person&min-age=18&marital-status=MARRIED&marital-status=SINGLE&signed-up=last-month&subscription-type=premium&subscription-type=plus"
                )
            ) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"),
                "minAge".gte(18),
                Compound.or("maritalStatus" eq MaritalStatus.MARRIED, "maritalStatus" eq MaritalStatus.SINGLE),
                "signupDate".gte(LocalDate.now().minusDays(30)),
                Compound.and("subscriptionType" eq "premium", "subscriptionType" eq "plus")
            )
        }

        "query params to filter - AND" {
            val filter = queryValuesFilter(DataFilter.Compound.Operator.AND) {
                Query.multi.optional("type").or { "entity-type".eq(it) }
                Query.int().optional("min-age").filter { "minAge".gte(it) }
                Query.enum<MaritalStatus>().multi.optional("marital-status").or { "maritalStatus".eq(it) }
                Query.optional("signed-up").filter { value ->
                    when (value) {
                        "last-month" -> "signupDate".gte(LocalDate.now().minusDays(30))
                        "last-6-months" -> "signupDate".gte(LocalDate.now().minusDays(180))
                        else -> null
                    }
                }
                Query.multi.optional("subscription-type").and {
                    "subscriptionType" eq it
                }
            }

            filter(
                Request(
                    Method.GET,
                    "/test?type=person&min-age=18&marital-status=MARRIED&marital-status=SINGLE&signed-up=last-month&subscription-type=premium&subscription-type=plus"
                )
            ) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"),
                "minAge".gte(18),
                Compound.or("maritalStatus" eq MaritalStatus.MARRIED, "maritalStatus" eq MaritalStatus.SINGLE),
                "signupDate".gte(LocalDate.now().minusDays(30)),
                Compound.and("subscriptionType" eq "premium", "subscriptionType" eq "plus")
            )
        }

        "query params to filter - missing" {
            val filter = queryValuesFilter {
                Query.multi.optional("type").or { "entity-type".eq(it) }
                Query.optional("include-empty-docs").whenMissing { "doc.size".gt(0) }
            }

            filter(Request(Method.GET, "/test?type=person")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person"), DataFilter.Field.gt("doc.size", 0)
            )

            filter(Request(Method.GET, "/test?type=person&include-empty-docs=nothing")) shouldBe Compound.and(
                Compound.or("entity-type" eq "person")
            )
        }
    }

    enum class MaritalStatus {
        MARRIED,
        SINGLE,
        CURRENTLY_SEPARATED
    }
}
