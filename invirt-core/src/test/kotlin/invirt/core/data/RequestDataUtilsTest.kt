package invirt.core.data

import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import io.kotest.core.spec.style.StringSpec
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
})
