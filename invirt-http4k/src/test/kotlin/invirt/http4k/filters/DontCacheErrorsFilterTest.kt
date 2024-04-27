package invirt.http4k.filters

import invirt.http4k.GET
import invirt.http4k.cacheOneYear
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveHeader
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class DontCacheErrorsFilterTest : StringSpec({

    "errors don't allow cache" {
        val handler = DontCacheErrorsFilter()
            .then(cacheOneYear())
            .then(routes("/test" GET { Response(Status.NOT_FOUND) }))

        val response = handler(Request(Method.GET, "/test"))
        response shouldHaveStatus Status.NOT_FOUND
        response.shouldHaveHeader("Cache-Control", "no-cache")
    }

    "non-errors allow cache" {
        val handler = DontCacheErrorsFilter()
            .then(cacheOneYear())
            .then(routes("/test" GET { Response(Status.OK) }))

        val response = handler(Request(Method.GET, "/test"))
        response shouldHaveStatus Status.OK
        response.shouldHaveHeader("Cache-Control", "public, max-age=31536000")
    }

    "custom statuses" {
        val handler = DontCacheErrorsFilter(Status.INTERNAL_SERVER_ERROR)
            .then(cacheOneYear())
            .then(routes("/test" GET { Response(Status.INTERNAL_SERVER_ERROR) }))

        val response = handler(Request(Method.GET, "/test"))
        response shouldHaveStatus Status.INTERNAL_SERVER_ERROR
        response.shouldHaveHeader("Cache-Control", "no-cache")
    }
})
