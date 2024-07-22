package invirt.http4k.security.authentication

import invirt.http4k.InvirtFilter
import invirt.http4k.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContext
import java.util.*

class RequestPrincipalTest : StringSpec({

    "set/get/clear" {
        val id = UUID.randomUUID()
        val context = RequestContext(id)
        val request = Request(Method.GET, "/").header("x-http4k-context", id.toString())
        InvirtFilter.requestContexts.inject(context, request)

        val principal = TestPrincipal(uuid7())

        request.setPrincipal(principal)
        Principal.present shouldBe true
        Principal.current shouldBe principal
        request.principal shouldBe principal

        request.clearPrincipal()
        Principal.present shouldBe false
        Principal.currentSafe shouldBe null
        request.principal shouldBe null
        shouldThrow<NullPointerException> {
            Principal.current
        }
    }
})
