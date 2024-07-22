package invirt.http4k.security.authentication

import invirt.http4k.InvirtFilter
import invirt.http4k.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContext
import java.util.*

class PrincipalTest : StringSpec({

    "useOnThisThread clears principal even when an exception is thrown" {
        val principal = TestPrincipal(uuid7())
        lateinit var fromThread: Principal
        var fromThreadSafe: Principal? = null
        lateinit var fromThreadTyped: TestPrincipal
        shouldThrow<IllegalStateException> {
            principal.useOnThisThread {
                fromThread = Principal.current
                fromThreadSafe = Principal.currentSafe
                fromThreadTyped = Principal.current as TestPrincipal
                throw IllegalStateException("Error")
            }
        }

        fromThread shouldBe principal
        fromThreadSafe shouldBe principal
        fromThreadTyped shouldBe principal

        Principal.isPresent shouldBe false
        Principal.currentSafe shouldBe null
        shouldThrowWithMessage<IllegalStateException>("No Principal found on current threads") {
            Principal.current
        }
    }

    "useOnThreadAndRequest" {
        val id = UUID.randomUUID()
        val context = RequestContext(id)
        val request = Request(Method.GET, "/").header("x-http4k-context", id.toString())
        InvirtFilter.requestContexts.inject(context, request)

        val principal = TestPrincipal(uuid7())

        lateinit var fromThread: Principal
        var fromThreadSafe: Principal? = null
        lateinit var fromThreadTyped: TestPrincipal
        lateinit var fromRequest: Principal

        principal.useOnThreadAndRequest(request) {
            fromThread = Principal.current
            fromThreadSafe = Principal.currentSafe
            fromThreadTyped = Principal.current as TestPrincipal
            fromRequest = request.principal!!
            Principal.isPresent shouldBe true
        }

        fromThread shouldBe principal
        fromThreadSafe shouldBe principal
        fromThreadTyped shouldBe principal
        fromRequest shouldBe principal

        Principal.isPresent shouldBe false
        Principal.currentSafe shouldBe null
        request.principal shouldBe null
        shouldThrowWithMessage<IllegalStateException>("No Principal found on current threads") {
            Principal.current
        }
    }
})
