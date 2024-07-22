package invirt.http4k.security

import invirt.http4k.GET
import invirt.http4k.security.authentication.Principal
import invirt.http4k.security.authentication.securedRoutes
import io.kotest.core.spec.style.StringSpec
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class SecuredRoutesTest : StringSpec({

    "allowed" {
        val handler = routes(
            securedRoutes(
                { "ADMIN" in it.roles },
                routes(
                    "/admin" GET { Response(Status.OK) },
                    "/admin/test" GET { Response(Status.OK) }
                )
            )
        )

        withRoles("ADMIN") {
            handler(Request(Method.GET, "/admin")) shouldHaveStatus Status.OK
            handler(Request(Method.GET, "/admin/test")) shouldHaveStatus Status.OK
        }
    }

    "not allowed" {
        val permissionChecker: (Principal) -> Boolean = { principal ->
            "ADMIN" in principal.roles
        }
        val handler = securedRoutes(
            permissionChecker,
            routes(
                "/admin" GET { Response(Status.OK) },
                "/admin/test" GET { Response(Status.OK) }
            )
        )

        withRoles("USER") {
            handler(Request(Method.GET, "/admin")) shouldHaveStatus Status.FORBIDDEN
            handler(Request(Method.GET, "/admin/test")) shouldHaveStatus Status.FORBIDDEN
        }
    }

    "block if no principal present" {
        val handler = routes(
            securedRoutes(
                { "ADMIN" in it.roles },
                routes(
                    "/admin" GET { Response(Status.OK) }
                )
            )
        )
        handler(Request(Method.GET, "/admin")) shouldHaveStatus Status.FORBIDDEN
    }

    "allow if public endpoint" {
        val handler = routes(
            securedRoutes(
                { "ADMIN" in it.roles },
                routes(
                    "/admin" GET { Response(Status.OK) }
                )
            ),
            "/public/{param}" GET { Response(Status.OK) }
        )

        // Still forbidden on /admin if you have no role
        handler(Request(Method.GET, "/admin")) shouldHaveStatus Status.FORBIDDEN

        // But you can access the public endpoint
        handler(Request(Method.GET, "/public/1")) shouldHaveStatus Status.OK
    }

    "multiple roles - allowed if you have the right one" {
        val check: (Principal) -> Boolean = { "USER" in it.roles || "ADMIN" in it.roles }
        val testRoutes = routes("/test" GET { Response(Status.OK) })
        val request = Request(Method.GET, "/test")

        // Someone with role "ADMIN" is allowed
        withRoles("ADMIN") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.OK
        }

        // Someone with role "USER" is allowed
        withRoles("USER") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.OK
        }

        // Someone with either "ADMIN" or "USER" is allowed
        withRoles("USER", "HR", "CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.OK
        }
        withRoles("ADMIN", "HR", "CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.OK
        }
        withRoles("ADMIN", "USER", "HR", "CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.OK
        }
    }

    "multiple roles - not allowed if you don't have the right one" {
        val check: (Principal) -> Boolean = { "USER" in it.roles || "ADMIN" in it.roles }
        val testRoutes = routes("/test" GET { Response(Status.OK) })
        val request = Request(Method.GET, "/test")

        withRoles("HR") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.FORBIDDEN
        }
        withRoles("CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.FORBIDDEN
        }
        withRoles("HR", "IT", "CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.FORBIDDEN
        }
        withRoles("ADMINISTRATOR", "HR", "CEO") {
            securedRoutes(check, testRoutes)(request) shouldHaveStatus Status.FORBIDDEN
        }
    }
})
