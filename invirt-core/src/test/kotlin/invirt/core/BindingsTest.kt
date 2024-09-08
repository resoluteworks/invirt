package invirt.core

import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class BindingsTest : StringSpec({

    "GET" {
        routes("/test" GET { Response(Status.OK) })(Request(Method.GET, "/test")) shouldHaveStatus Status.OK
    }

    "POST" {
        routes("/test" POST { Response(Status.OK) })(Request(Method.POST, "/test")) shouldHaveStatus Status.OK
    }

    "PUT" {
        routes("/test" PUT { Response(Status.OK) })(Request(Method.PUT, "/test")) shouldHaveStatus Status.OK
    }

    "DELETE" {
        routes("/test" DELETE { Response(Status.OK) })(Request(Method.DELETE, "/test")) shouldHaveStatus Status.OK
    }
})
