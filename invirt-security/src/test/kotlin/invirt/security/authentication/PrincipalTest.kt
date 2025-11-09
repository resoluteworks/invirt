package invirt.security.authentication

import invirt.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request

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
        shouldThrowWithMessage<IllegalStateException>("No Principal found on current thread") {
            Principal.current
        }
    }

    "withPrincipal should set the principal on the request" {
        val principal = TestPrincipal(uuid7())
        val request = Request(Method.GET, "/test").withPrincipal(principal)

        request.principal shouldBe principal
        request.hasPrincipal shouldBe true
    }

    "hasPrincipal should be false when no principal is set" {
        val request = Request(Method.GET, "/test")

        request.principal shouldBe null
        request.hasPrincipal shouldBe false
    }
})
