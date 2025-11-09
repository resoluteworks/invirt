package invirt.security.authentication

import invirt.core.InvirtRequest
import invirt.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request

class PrincipalTest : StringSpec({

    "withPrincipal should set the principal on the request" {
        val principal = TestPrincipal(uuid7())
        val request = Request(Method.GET, "/test").withPrincipal(principal)

        request.principal shouldBe principal
        request.hasPrincipal shouldBe true
    }

    "principal should be fetched correctly on an InvirtRequest" {
        val principal = TestPrincipal(uuid7())
        val request = InvirtRequest(Request(Method.GET, "/test").withPrincipal(principal))

        request.principal shouldBe principal
        request.hasPrincipal shouldBe true
    }

    "hasPrincipal should be false when no principal is set" {
        val request = Request(Method.GET, "/test")

        request.principal shouldBe null
        request.hasPrincipal shouldBe false
    }
})
