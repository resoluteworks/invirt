package invirt.http4k

import invirt.data.Page
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri

class UriTest : StringSpec({

    "removeQueryValue" {
        Uri.of("/test").removeQueryValue("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=John").removeQueryValue("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=nothing&from=0&size=1").removeQueryValue("q", "nothing").toString() shouldBe "/test?from=0&size=1"

        Uri.of("/test?q=nothing&from=0&filter=one&size=1&filter=two").removeQueryValue("filter", "one")
            .toString() shouldBe "/test?q=nothing&from=0&size=1&filter=two"
    }

    "removeQueries" {
        Uri.of("/test").removeQueries(listOf("q", "filter")).toString() shouldBe "/test"
        Uri.of("/test?q=john&filter=name").removeQueries(listOf("q", "filter")).toString() shouldBe "/test"
        Uri.of("/test?q=john&filter=name&size=10").removeQueries(listOf("q", "filter")).toString() shouldBe "/test?size=10"
    }

    "replacePage" {
        Uri.of("/test").replacePage(Page(20, 10)).toString() shouldBe "/test?from=20&size=10"
        Uri.of("/test?q=john&filter=name").removeQueries(listOf("q", "filter")).toString() shouldBe "/test"
        Uri.of("/test?q=john&filter=name&size=10").removeQueries(listOf("q", "filter")).toString() shouldBe "/test?size=10"
    }

    "toggleQuery" {
        Uri.of("/test").toggleQueryValue("q", "John").toString() shouldBe "/test?q=John"
        Uri.of("/test?q=John").toggleQueryValue("q", "John").toString() shouldBe "/test"

        Uri.of("/test?q=nothing&from=0&filter=one&size=1&filter=two").toggleQueryValue("filter", "one")
            .toString() shouldBe "/test?q=nothing&from=0&size=1&filter=two"

        Uri.of("/test?q=nothing&from=0&filter=one&size=1").toggleQueryValue("filter", "two")
            .toString() shouldBe "/test?q=nothing&from=0&filter=one&size=1&filter=two"
    }

    "replaceQuery" {
        Uri.of("/test").replaceQuery("q" to "John Smith").toString() shouldBe "/test?q=John+Smith"
        Uri.of("/test").replaceQuery("q" to "John", "size" to "10").toString() shouldBe "/test?q=John&size=10"
        Uri.of("/test?q=nothing&size=5").replaceQuery("q" to "John", "size" to "10").toString() shouldBe "/test?q=John&size=10"
        Uri.of("/test?q=nothing&size=5").replaceQuery("size" to "10", "q" to "John").toString() shouldBe "/test?size=10&q=John"
        Uri.of("/test?q=nothing&from=0&size=1").replaceQuery("size" to "5", "q" to "John").toString() shouldBe "/test?from=0&size=5&q=John"
        Uri.of("/test?q=nothing&from=0&size=1").replaceQuery("size" to "5", "from" to "100").toString() shouldBe "/test?q=nothing&size=5&from=100"
    }
})
