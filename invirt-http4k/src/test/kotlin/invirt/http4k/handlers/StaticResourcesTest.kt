package invirt.http4k.handlers

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.and
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.kotest.haveBody
import org.http4k.kotest.haveStatus

class StaticResourcesTest : StringSpec() {

    init {
        "classpath" {
            val httpHandler = StaticResources.Classpath("1", "static-resources-test/static")
            httpHandler(
                Request(
                    Method.GET,
                    "/static/assets/1/css/style.css"
                )
            ) should (haveStatus(Status.OK) and haveBody(".class{}"))
            httpHandler(
                Request(
                    Method.GET,
                    "/static/static-file.txt"
                )
            ) should (haveStatus(Status.OK) and haveBody("This is a static file"))
            httpHandler(
                Request(
                    Method.GET,
                    "/static/some/nested/nested-file.txt"
                )
            ) should (haveStatus(Status.OK) and haveBody("Nested file"))
        }

        "hotreload" {
            val httpHandler = StaticResources.HotReload("2", "src/test/resources/static-resources-test/static")
            httpHandler(
                Request(
                    Method.GET,
                    "/static/assets/2/css/style.css"
                )
            ) should (haveStatus(Status.OK) and haveBody(".class{}"))
            httpHandler(
                Request(
                    Method.GET,
                    "/static/static-file.txt"
                )
            ) should (haveStatus(Status.OK) and haveBody("This is a static file"))
            httpHandler(
                Request(
                    Method.GET,
                    "/static/some/nested/nested-file.txt"
                )
            ) should (haveStatus(Status.OK) and haveBody("Nested file"))
        }

        "caching" {
            val httpHandler = StaticResources.HotReload("3", "src/test/resources/static-resources-test/static")
            httpHandler(
                Request(
                    Method.GET,
                    "/static/assets/3/css/style.css"
                )
            ).header("Cache-Control") shouldBe "public, max-age=31536000"
        }
    }
}
