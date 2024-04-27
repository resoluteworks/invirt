package invirt.http4k.security.authentication

import invirt.http4k.AppRequestContexts
import invirt.http4k.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContext
import java.util.*

class RequestAuthTest : StringSpec({

    "set/get/clear" {
        val id = UUID.randomUUID()
        val context = RequestContext(id)
        val request = Request(Method.GET, "/").header("x-http4k-context", id.toString())
        AppRequestContexts.contexts.inject(context, request)

        val principal = TestPrincipal(uuid7())
        val tokens = AuthTokens { emptyList() }

        request.authentication = Authentication(principal, tokens)
        request.authentication shouldBe Authentication(principal, tokens)
        Authentication.current shouldBe Authentication(principal, tokens)
        Principal.current shouldBe principal
        request.principal shouldBe principal

        request.authentication = null
        request.authentication shouldBe null
        Authentication.current shouldBe null
        Principal.currentSafe shouldBe null
        request.principal shouldBe null
    }
})
