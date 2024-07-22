package invirt.http4k.security.authentication

import invirt.http4k.security.TestPrincipal
import invirt.utils.uuid7
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PrincipalTest : StringSpec({

    "useOnThisThread" {
        val principal = TestPrincipal(uuid7())
        lateinit var fromThread: Principal
        var fromThreadSafe: Principal? = null
        lateinit var fromThreadTyped: TestPrincipal
        shouldThrow<IllegalStateException> {
            principal.useOnThisThread {
                fromThread = Principal.current
                fromThreadTyped = Principal.current<TestPrincipal>()
                throw IllegalStateException("Error")
            }
        }

        fromThread shouldBe principal
        fromThreadSafe shouldBe principal
        fromThreadTyped shouldBe principal

        Principal.present shouldBe false
        shouldThrow<NullPointerException> {
            Principal.current
        }
        shouldThrow<NullPointerException> {
            Principal.current<TestPrincipal>()
        }
    }
})
