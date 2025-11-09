package invirt.security

import invirt.core.GET
import invirt.security.authentication.securedRoutes
import invirt.security.authentication.withPrincipal
import invirt.utils.uuid7
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
            securedRoutes<TestPrincipal>(
                { "ADMIN" in it.roles },
                routes(
                    "/admin" GET { Response(Status.OK) },
                    "/admin/test" GET { Response(Status.OK) }
                )
            )
        )

        val principal = TestPrincipal(uuid7(), attributes = mapOf("roles" to setOf("ADMIN")))
        handler(Request(Method.GET, "/admin").withPrincipal(principal)) shouldHaveStatus Status.OK
        handler(Request(Method.GET, "/admin/test").withPrincipal(principal)) shouldHaveStatus Status.OK
    }

    "not allowed" {
        val permissionChecker: (TestPrincipal) -> Boolean = { principal ->
            "ADMIN" in principal.roles
        }
        val handler = securedRoutes(
            permissionChecker,
            routes(
                "/admin" GET { Response(Status.OK) },
                "/admin/test" GET { Response(Status.OK) }
            )
        )

        val principal = TestPrincipal(uuid7(), attributes = mapOf("roles" to setOf("USER")))

        handler(Request(Method.GET, "/admin").withPrincipal(principal)) shouldHaveStatus Status.FORBIDDEN
        handler(Request(Method.GET, "/admin/test").withPrincipal(principal)) shouldHaveStatus Status.FORBIDDEN
    }

    "block if no principal present" {
        val handler = routes(
            securedRoutes<TestPrincipal>(
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
            securedRoutes<TestPrincipal>(
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
        val check: (TestPrincipal) -> Boolean = { "USER" in it.roles || "ADMIN" in it.roles }
        val testRoutes = routes("/test" GET { Response(Status.OK) })
        val request = Request(Method.GET, "/test")

        // Someone with role "ADMIN" is allowed
        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("ADMIN"))
                )
            )
        ) shouldHaveStatus Status.OK

        // Someone with role "USER" is allowed
        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("USER"))
                )
            )
        ) shouldHaveStatus Status.OK

        // Someone with either "ADMIN" or "USER" is allowed
        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(uuid7(), attributes = mapOf("roles" to setOf("USER", "HR", "CEO")))
            )
        ) shouldHaveStatus Status.OK

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(uuid7(), attributes = mapOf("roles" to setOf("ADMIN", "HR", "CEO")))
            )
        ) shouldHaveStatus Status.OK

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(uuid7(), attributes = mapOf("roles" to setOf("ADMIN", "USER", "HR", "CEO")))
            )
        ) shouldHaveStatus Status.OK
    }

    "multiple roles - not allowed if you don't have the right one" {
        val check: (TestPrincipal) -> Boolean = { "USER" in it.roles || "ADMIN" in it.roles }
        val testRoutes = routes("/test" GET { Response(Status.OK) })
        val request = Request(Method.GET, "/test")

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("HR"))
                )
            )
        ) shouldHaveStatus Status.FORBIDDEN

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("CEO"))
                )
            )
        ) shouldHaveStatus Status.FORBIDDEN

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("HR", "IT", "CEO"))
                )
            )
        ) shouldHaveStatus Status.FORBIDDEN

        securedRoutes(check, testRoutes)(
            request.withPrincipal(
                TestPrincipal(
                    uuid7(),
                    attributes = mapOf("roles" to setOf("ADMINISTRATOR", "HR", "CEO"))
                )
            )
        ) shouldHaveStatus Status.FORBIDDEN
    }
})
