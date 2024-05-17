package invirt.http4k

import invirt.data.Filter
import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request

class DataTest : StringSpec({

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

    "Request.filters()" {
        Request(Method.GET, "/test").filters("field1", "field2") shouldBe emptyList()
        Request(Method.GET, "/test?size=large&dob=gte:today").filters("size", "dob") shouldBe listOf(
            Filter.eq("size", "large"),
            Filter.gte("dob", "today")
        )
        Request(Method.GET, "/test?size=gte:10&size=lt:100").filters("size", "dob", "length") shouldBe listOf(
            Filter.gte("size", "10"),
            Filter.lt("size", "100")
        )
        Request(Method.GET, "/test?size=eq:10&status=ne:open").filters("size", "dob", "status", "length") shouldBe listOf(
            Filter.eq("size", "10"),
            Filter.ne("status", "open")
        )
    }
})
