package invirt.http4k.handlers

import invirt.http4k.cacheDays
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.and
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.haveBody
import org.http4k.kotest.haveStatus
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.routing.routes

class StaticAssetsTest : StringSpec({

    "classpath" {
        val httpHandler = routes(
            "/static/1" bind staticAssets(false, classpathLocation = "static-assets-test/static")
        )
        httpHandler(Request(Method.GET, "/static/1/css/style.css")) should (haveStatus(Status.OK) and haveBody(".class{}"))
        httpHandler(Request(Method.GET, "/static/1/some/nested/nested-file.txt")) should (haveStatus(Status.OK) and haveBody("Nested file"))

        httpHandler(Request(Method.GET, "/static/x/css/style.css")) shouldHaveStatus Status.NOT_FOUND
        httpHandler(Request(Method.GET, "/static/css/style.css")) shouldHaveStatus Status.NOT_FOUND
    }

    "hotreload" {
        val httpHandler = routes(
            "/static/2" bind staticAssets(true, directory = "src/test/resources/static-assets-test/static")
        )
        httpHandler(Request(Method.GET, "/static/2/css/style.css")) should (haveStatus(Status.OK) and haveBody(".class{}"))
        httpHandler(Request(Method.GET, "/static/2/some/nested/nested-file.txt")) should (haveStatus(Status.OK) and haveBody("Nested file"))

        httpHandler(Request(Method.GET, "/static/x/css/style.css")) shouldHaveStatus Status.NOT_FOUND
        httpHandler(Request(Method.GET, "/static/css/style.css")) shouldHaveStatus Status.NOT_FOUND
    }

    "hotreload from boolean" {
        fun test(hotReload: Boolean) {
            val httpHandler = routes(
                "/static/12345" bind staticAssets(
                    hotReload = hotReload,
                    directory = "src/test/resources/static-assets-test/static",
                    classpathLocation = "static-assets-test/static"
                )
            )
            httpHandler(Request(Method.GET, "/static/12345/css/style.css")) should (haveStatus(Status.OK) and haveBody(".class{}"))
            httpHandler(
                Request(
                    Method.GET,
                    "/static/12345/some/nested/nested-file.txt"
                )
            ) should (haveStatus(Status.OK) and haveBody("Nested file"))

            httpHandler(Request(Method.GET, "/static/77777/css/style.css")) shouldHaveStatus Status.NOT_FOUND
            httpHandler(Request(Method.GET, "/static/css/style.css")) shouldHaveStatus Status.NOT_FOUND
        }
        test(true)
        test(false)
    }

    "caching" {
        val httpHandler = routes(
            "/static/100" bind cacheDays(365).then(staticAssets(true, directory = "src/test/resources/static-assets-test/static"))
        )

        httpHandler(Request(Method.GET, "/static/100/css/style.css")).header("Cache-Control") shouldBe "public, max-age=31536000"
    }
})
