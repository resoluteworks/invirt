package invirt.security

import invirt.core.GET
import invirt.security.authentication.authenticatedRoutes
import invirt.security.authentication.withPrincipal
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class AuthenticatedRoutesTest : StringSpec({

    "allow if principal present" {
        val handler = routes(
            authenticatedRoutes(
                routes(
                    "/test" GET { Response(Status.OK) }
                ),
                routes(
                    "/dashboard" GET { Response(Status.ACCEPTED) }
                )
            )
        )

        handler(Request(Method.GET, "/test").withPrincipal(TestPrincipal(uuid7()))) shouldHaveStatus Status.OK
        handler(Request(Method.GET, "/dashboard").withPrincipal(TestPrincipal(uuid7()))) shouldHaveStatus Status.ACCEPTED
    }

    "block if no principal present" {
        val handler = routes(
            authenticatedRoutes(
                routes(
                    "/test" GET { Response(Status.OK) }
                )
            )
        )
        handler(Request(Method.GET, "/test")) shouldHaveStatus Status.FORBIDDEN
    }
})
