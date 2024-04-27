package invirt.utils

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ThreadLocalTest : StringSpec({

    "withValue" {
        val threadLocal = ThreadLocal<String>()
        var fromThread: String? = null
        threadLocal.withValue("test") {
            fromThread = threadLocal.get()
        }
        fromThread shouldBe "test"
        threadLocal.get() shouldBe null
    }

    "withValue - exception" {
        val threadLocal = ThreadLocal<String>()
        shouldThrow<IllegalStateException> {
            threadLocal.withValue("test") {
                threadLocal.get() shouldBe "test"
                throw IllegalStateException("")
            }
        }
        threadLocal.get() shouldBe null
    }
})
