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

    "Request.page() aligns the offset down to a multiple of the size, since Page requires one" {
        Request(Method.GET, "/test?from=15").page() shouldBe Page(10, 10)
        Request(Method.GET, "/test?from=30&size=30").page(0, 20, 20) shouldBe Page(20, 20)
        Request(Method.GET, "/test?from=-25").page() shouldBe Page(0, 10)
    }

    "Request.page() falls back to the default size for a size below 1" {
        Request(Method.GET, "/test?size=0").page() shouldBe Page(0, 10)
        Request(Method.GET, "/test?from=20&size=-3").page(0, 20, 20) shouldBe Page(20, 20)
    }

    "Request.sort()" {
        Request(Method.GET, "/test").sort() shouldBe null
        Request(Method.GET, "/test?sort=field").sort() shouldBe Sort("field", SortOrder.ASC)
        Request(Method.GET, "/test?sort=field:ASC").sort() shouldBe Sort("field", SortOrder.ASC)
        Request(Method.GET, "/test?sort=field:DESC").sort() shouldBe Sort("field", SortOrder.DESC)
    }
})
