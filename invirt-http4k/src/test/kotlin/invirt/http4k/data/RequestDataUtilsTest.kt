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

    "Request.selectedFilters()" {
        val options = listOf(
            stringFilterOption("status", listOf("enabled", "disabled"), CompoundCriteria.Operator.OR),
            intFilterOption("age", CompoundCriteria.Operator.AND),
            stringFilterOption("type", listOf("person", "company"), CompoundCriteria.Operator.OR),
            enumFilterOption<MaritalStatus>("marital-status", CompoundCriteria.Operator.OR)
        )

        Request(Method.GET, "/test").selectedFilters(options) shouldBe emptyList()

        Request(Method.GET, "/test?type=person&age=gte:18").selectedFilters(options) shouldContainExactlyInAnyOrder
            listOf(
                FieldCriteria.gte("age", 18),
                FieldCriteria.eq("type", "person")
            )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100").selectedFilters(options) shouldContainExactlyInAnyOrder
            listOf(
                FieldCriteria.gt("age", 18),
                FieldCriteria.lt("age", 100),
                FieldCriteria.eq("type", "person")
            )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100&marital-status=married&marital-status=single")
            .selectedFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldCriteria.gt("age", 18),
            FieldCriteria.lt("age", 100),
            FieldCriteria.eq("type", "person"),
            FieldCriteria.eq("marital-status", MaritalStatus.MARRIED),
            FieldCriteria.eq("marital-status", MaritalStatus.SINGLE)
        )

        Request(
            Method.GET,
            "/test?type=person&age=gt:18&age=lt:100&marital-status=mArried&size=100&marital-status=currently-separated&from=0"
        ).selectedFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldCriteria.gt("age", 18),
            FieldCriteria.lt("age", 100),
            FieldCriteria.eq("type", "person"),
            FieldCriteria.eq("marital-status", MaritalStatus.MARRIED),
            FieldCriteria.eq("marital-status", MaritalStatus.CURRENTLY_SEPARATED)
        )

        Request(Method.GET, "/test?type=person&type=company&status=enabled").selectedFilters(options) shouldContainExactlyInAnyOrder listOf(
            FieldCriteria.eq("status", "enabled"),
            FieldCriteria.eq("type", "person"),
            FieldCriteria.eq("type", "company")
        )
    }

    "Request.filterCriteria()" {
        val options = listOf(
            stringFilterOption("status", listOf("enabled", "disabled"), CompoundCriteria.Operator.OR),
            intFilterOption("age", CompoundCriteria.Operator.AND),
            stringFilterOption("type", listOf("person", "company"), CompoundCriteria.Operator.OR),
            enumFilterOption<MaritalStatus>("marital-status", CompoundCriteria.Operator.OR)
        )

        Request(Method.GET, "/test").filterCriteria(options) shouldBe null

        Request(Method.GET, "/test?type=person&age=gte:18").filterCriteria(options) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(FieldCriteria.gte("age", 18), FieldCriteria.eq("type", "person"))
        )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100").filterCriteria(options) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(
                CompoundCriteria.and(FieldCriteria.gt("age", 18), FieldCriteria.lt("age", 100)),
                FieldCriteria.eq("type", "person")
            )
        )

        Request(Method.GET, "/test?type=person&age=gt:18&age=lt:100&marital-status=married&marital-status=single")
            .filterCriteria(options) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(
                CompoundCriteria.and(FieldCriteria.gt("age", 18), FieldCriteria.lt("age", 100)),
                FieldCriteria.eq("type", "person"),
                CompoundCriteria.or(
                    FieldCriteria.eq("marital-status", MaritalStatus.MARRIED),
                    FieldCriteria.eq("marital-status", MaritalStatus.SINGLE)
                )
            )
        )

        Request(
            Method.GET,
            "/test?type=person&age=gt:18&age=lt:100&marital-status=mArried&size=100&marital-status=currently-separated&from=0"
        )
            .filterCriteria(options) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(
                CompoundCriteria.and(FieldCriteria.gt("age", 18), FieldCriteria.lt("age", 100)),
                FieldCriteria.eq("type", "person"),
                CompoundCriteria.or(
                    FieldCriteria.eq("marital-status", MaritalStatus.MARRIED),
                    FieldCriteria.eq("marital-status", MaritalStatus.CURRENTLY_SEPARATED)
                )
            )
        )

        Request(Method.GET, "/test?type=person&type=company&status=enabled")
            .filterCriteria(options) shouldBe CompoundCriteria(
            CompoundCriteria.Operator.AND,
            listOf(
                FieldCriteria.eq("status", "enabled"),
                CompoundCriteria.or(FieldCriteria.eq("type", "person"), FieldCriteria.eq("type", "company"))
            )
        )
    }
})

enum class MaritalStatus {
    MARRIED,
    SINGLE,
    CURRENTLY_SEPARATED
}
