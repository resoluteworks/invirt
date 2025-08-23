package invirt.core.filters

import invirt.core.Invirt
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then

class DownForMaintenanceFilterTest : StringSpec() {

    init {
        "should return 503 when maintenance mode is on" {
            val handler = Invirt()
                .then(DownForMaintenanceFilter("down-for-maintenance.peb", setOf("/admin/*", "/health")) { true })
                .then { Response(Status.OK) }

            // Non-excluded paths should return 503
            handler.shouldBeDown("/some-path")
            handler.shouldBeDown("/user-dashboard")
            handler.shouldBeDown("/admin")

            // Excluded paths should return 200
            handler.shouldBeUp("/admin/dashboard")
            handler.shouldBeUp("/health")
        }

        "should return 200 when maintenance mode is off" {
            val handler = Invirt()
                .then(DownForMaintenanceFilter("down-for-maintenance.peb", setOf("/admin/*", "/health")) { false })
                .then { Response(Status.OK) }

            // All paths should return 200
            handler.shouldBeUp("/some-path")
            handler.shouldBeUp("/user-dashboard")
            handler.shouldBeUp("/admin")
            handler.shouldBeUp("/admin/dashboard")
            handler.shouldBeUp("/health")
            handler.shouldBeUp("/admin/something")
        }
    }

    private fun HttpHandler.shouldBeDown(path: String) {
        val response = this(Request(Method.GET, path))
        response.status shouldBe Status.SERVICE_UNAVAILABLE
        response.bodyString().trim() shouldBe "This website is currently down for maintenance. Please check back later."
    }

    private fun HttpHandler.shouldBeUp(path: String) {
        val response = this(Request(Method.GET, path))
        response.status shouldBe Status.OK
    }
}
