package invirt.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.routes

class CachingTest : StringSpec({

    "cacheDays" {
        val handler = cacheDays(10).then(routes("/test" GET { Response(Status.OK) }))
        handler(Request(Method.GET, "/test")).header("Cache-Control") shouldBe "public, max-age=864000"
    }

    "cacheOneYear" {
        val handler = cacheOneYear().then(routes("/test" GET { Response(Status.OK) }))
        handler(Request(Method.GET, "/test")).header("Cache-Control") shouldBe "public, max-age=31536000"
    }
})
