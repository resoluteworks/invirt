package invirt.core

import invirt.data.Page
import invirt.data.Sort
import invirt.data.SortOrder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri

class UriTest : StringSpec({

    "removeQueryValue" {
        Uri.of("/test").removeQueryValue("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=John").removeQueryValue("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=nothing&from=0&size=1").removeQueryValue("q", "nothing").toString() shouldBe "/test?from=0&size=1"
        Uri.of("/test?q=kotlin&q=java").removeQueryValue("q", "java").toString() shouldBe "/test?q=kotlin"

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
        Uri.of("/test?from=0&size=10").replacePage(Page(10, 5)).toString() shouldBe "/test?from=10&size=5"
        Uri.of("/test?q=john&filter=name").removeQueries(listOf("q", "filter")).toString() shouldBe "/test"
        Uri.of("/test?q=john&filter=name&size=10").removeQueries(listOf("q", "filter")).toString() shouldBe "/test?size=10"
    }

    "replaceSort" {
        Uri.of("/test").replaceSort(Sort("name", SortOrder.ASC)).toString() shouldBe "/test?sort=name%3Aasc"
        Uri.of("/test?sort=name:Asc").replaceSort(Sort("createdAt", SortOrder.DESC)).toString() shouldBe "/test?sort=createdAt%3Adesc"

        Uri.of("/test?sort=name:Asc&from=10&size=10").replaceSort(Sort("createdAt", SortOrder.DESC))
            .toString() shouldBe "/test?sort=createdAt%3Adesc"

        Uri.of("/test?sort=name:Asc&from=10&size=10").replaceSort(Sort("createdAt", SortOrder.DESC), false)
            .toString() shouldBe "/test?from=10&size=10&sort=createdAt%3Adesc"
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
        Uri.of("/test?q=nothing&from=0&size=1").replaceQuery("size" to "5", "from" to "100")
            .toString() shouldBe "/test?q=nothing&size=5&from=100"
    }

    "hasQueryParam" {
        Uri.of("/test?q=nothing&from=0&size=1").hasQueryParam("q") shouldBe true
        Uri.of("/test?Q=nothing&from=0&size=1").hasQueryParam("q") shouldBe true
        Uri.of("/test?test&from=0&size=1").hasQueryParam("test") shouldBe true
        Uri.of("/test?test&test=something&from&size=1").hasQueryParam("from") shouldBe true
        Uri.of("/test?test&test=something&fRom&size=1").hasQueryParam("from") shouldBe true
        Uri.of("/test?test&test=something&from").hasQueryParam("size") shouldBe false
    }

    "hasQueryValue" {
        Uri.of("/test?q=nothing&from=0&size=1").hasQueryValue("q", "nothing") shouldBe true
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("test", "nothing") shouldBe true
        Uri.of("/test?test=nothing&tEsT=something&from=0&size=1").hasQueryValue("test", "nothing") shouldBe true
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("test", "something") shouldBe true
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("from", "0") shouldBe true
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("size", "1") shouldBe true
        Uri.of("/test?test=nothing&test=something&from=0&sIze=11").hasQueryValue("size", "11") shouldBe true

        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("size", "11") shouldBe false
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("test", "something,nothing") shouldBe false
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("page", "1") shouldBe false
        Uri.of("/test?test=nothing&test=something&from=0&size=1").hasQueryValue("from", "01") shouldBe false
    }

    "csvAppend" {
        Uri.of("/test").csvAppend("q", "John").toString() shouldBe "/test?q=John"
        Uri.of("/test?q=John").csvAppend("q", "Jane").toString() shouldBe "/test?q=John%2CJane"
        Uri.of("/test?q=John,Jane").csvAppend("q", "Jane").toString() shouldBe "/test?q=John%2CJane"
    }

    "csvRemove" {
        Uri.of("/test").csvRemove("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=John").csvRemove("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=John%2CJane").csvRemove("q", "Jane").toString() shouldBe "/test?q=John"
        Uri.of("/test?q=John,Jane").csvRemove("q", "John").toString() shouldBe "/test?q=Jane"
        Uri.of("/test?q=3,5").csvRemove("q", 3).toString() shouldBe "/test?q=5"
    }

    "csvToggle" {
        Uri.of("/test").csvToggle("q", "John").toString() shouldBe "/test?q=John"
        Uri.of("/test?q=John").csvToggle("q", "John").toString() shouldBe "/test"
        Uri.of("/test?q=John").csvToggle("q", "Jane").toString() shouldBe "/test?q=John%2CJane"
        Uri.of("/test?q=John%2CJane").csvToggle("q", "Jane").toString() shouldBe "/test?q=John"
        Uri.of("/test?q=John,Jane").csvToggle("q", "John").toString() shouldBe "/test?q=Jane"
        Uri.of("/test?q=1,2").csvToggle("q", 2).toString() shouldBe "/test?q=1"
    }
})
