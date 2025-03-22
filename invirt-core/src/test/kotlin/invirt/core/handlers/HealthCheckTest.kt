package invirt.core.handlers

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.format.Jackson.json
import org.http4k.kotest.shouldHaveStatus

class HealthCheckTest : StringSpec({

    "json" {
        val handler = HealthCheck.json()
        val response = handler(Request(Method.GET, "/health"))

        response shouldHaveStatus Status.OK
        response.json<HealthStatus>() shouldBe HealthStatus("healthy")
    }
})
