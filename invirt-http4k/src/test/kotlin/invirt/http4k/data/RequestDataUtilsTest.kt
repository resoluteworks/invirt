package invirt.http4k.data

import invirt.data.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request

class RequestDataUtilsTest : StringSpec({

    "Request.page()" {
        Request(Method.GET, "/test").page() shouldBe Page(0, 10)
        Request(Method.GET, "/test?from=100").page() shouldBe Page(100, 10)
        Request(Method.GET, "/test?from=40&size=10").page() shouldBe Page(40, 10)
        Request(Method.GET, "/test?from=0&size=10324").page() shouldBe Page(0, 10)
        Request(Method.GET, "/test?from=200&size=32432").page(maxSize = 50) shouldBe Page(200, 50)
    }

    "Request.sort()" {
        Request(Method.GET, "/test").sort() shouldBe null
        Request(Method.GET, "/test?sort=field").sort() shouldBe Sort("field", SortOrder.ASC)
        Request(Method.GET, "/test?sort=field:ASC").sort() shouldBe Sort("field", SortOrder.ASC)
        Request(Method.GET, "/test?sort=field:DESC").sort() shouldBe Sort("field", SortOrder.DESC)
    }

    "Request.queryFieldFilters()" {
        val options = listOf(
            stringFilterOption("status", listOf("enabled", "disabled")),
            intFilterOption("age"),
            stringFilterOption("type", listOf("person", "company")),
            enumFilterOption<MaritalStatus>("marital-status")
        )

        Request(Method.GET, "/test").queryFieldFilters(options) shouldBe emptyList()

        Request(Method.GET, "/test?type=person&age=gte:18").queryFieldFilters(options) shouldContainExactlyInAnyOrder
            listOf(
                FieldFilter.gte("age", 18),
                FieldFilter.eq("type", "person")
            )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100").queryFieldFilters(options) shouldContainExactlyInAnyOrder
            listOf(
                FieldFilter.gt("age", 18),
                FieldFilter.lt("age", 100),
                FieldFilter.eq("type", "person")
            )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100&marital-status=married&marital-status=single")
            .queryFieldFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldFilter.gt("age", 18),
            FieldFilter.lt("age", 100),
            FieldFilter.eq("type", "person"),
            FieldFilter.eq("marital-status", MaritalStatus.MARRIED),
            FieldFilter.eq("marital-status", MaritalStatus.SINGLE)
        )

        Request(
            Method.GET,
            "/test?type=person&age=gt:18&age=lt:100&marital-status=mArried&size=100&marital-status=currently-separated&from=0"
        ).queryFieldFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldFilter.gt("age", 18),
            FieldFilter.lt("age", 100),
            FieldFilter.eq("type", "person"),
            FieldFilter.eq("marital-status", MaritalStatus.MARRIED),
            FieldFilter.eq("marital-status", MaritalStatus.CURRENTLY_SEPARATED)
        )

        Request(Method.GET, "/test?type=person&type=company&status=enabled").queryFieldFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldFilter.eq("status", "enabled"),
            FieldFilter.eq("type", "person"),
            FieldFilter.eq("type", "company")
        )
    }

    "Request.queryParamsAsFilter()" {
        val options = listOf(
            stringFilterOption("status", listOf("enabled", "disabled")),
            intFilterOption("age"),
            stringFilterOption("type", listOf("person", "company")),
            enumFilterOption<MaritalStatus>("marital-status")
        )

        Request(Method.GET, "/test").queryParamsAsFilter(options) shouldBe null

        Request(Method.GET, "/test?type=person&age=gte:18").queryParamsAsFilter(options) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(FieldFilter.gte("age", 18), FieldFilter.eq("type", "person"))
        )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100").queryParamsAsFilter(options) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(
                CompoundFilter.and(FieldFilter.gt("age", 18), FieldFilter.lt("age", 100)),
                FieldFilter.eq("type", "person")
            )
        )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100&marital-status=married&marital-status=single")
            .queryParamsAsFilter(options) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(
                CompoundFilter.and(FieldFilter.gt("age", 18), FieldFilter.lt("age", 100)),
                FieldFilter.eq("type", "person"),
                CompoundFilter.or(
                    FieldFilter.eq("marital-status", MaritalStatus.MARRIED),
                    FieldFilter.eq("marital-status", MaritalStatus.SINGLE)
                )
            )
        )

        Request(
            Method.GET,
            "/test?type=person&age=gt:18&age=lt:100&marital-status=mArried&size=100&marital-status=currently-separated&from=0"
        )
            .queryParamsAsFilter(options) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(
                CompoundFilter.and(FieldFilter.gt("age", 18), FieldFilter.lt("age", 100)),
                FieldFilter.eq("type", "person"),
                CompoundFilter.or(
                    FieldFilter.eq("marital-status", MaritalStatus.MARRIED),
                    FieldFilter.eq("marital-status", MaritalStatus.CURRENTLY_SEPARATED)
                )
            )
        )

        Request(Method.GET, "/test?type=person&type=company&status=enabled")
            .queryParamsAsFilter(options) shouldBe CompoundFilter(
            CompoundFilter.Operator.AND,
            listOf(
                FieldFilter.eq("status", "enabled"),
                CompoundFilter.or(FieldFilter.eq("type", "person"), FieldFilter.eq("type", "company"))
            )
        )
    }
})

enum class MaritalStatus {
    MARRIED,
    SINGLE,
    CURRENTLY_SEPARATED
}
