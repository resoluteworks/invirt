package invirt.http4k.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request

class RequestQueryFiltersTest : StringSpec({

    "selected values" {
        val options = listOf(
            stringFilterOption("status", listOf("enabled", "disabled")),
            stringFilterOption("type", listOf("person", "company")),
            enumFilterOption<MaritalStatus>("marital-status")
        )

        val request = Request(
            Method.GET,
            "/test?type=person&marital-status=mArried&size=100&marital-status=currently-separated&from=0&status=enabled"
        )

        val filterStatus = RequestQueryFilters(request, options)
        filterStatus.selected("status", "enabled") shouldBe true
        filterStatus.selected("status", "disabled") shouldBe false
        filterStatus.selected("type", "person") shouldBe true
        filterStatus.selected("marital-status", MaritalStatus.MARRIED) shouldBe true
        filterStatus.selected("marital-status", MaritalStatus.CURRENTLY_SEPARATED) shouldBe true
        filterStatus.selected("marital-status", MaritalStatus.SINGLE) shouldBe false
    }
})
