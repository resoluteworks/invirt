package invirt.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class IdTest :
    StringSpec({

        "uuid7" {
            repeat(1000) {
                uuid7() shouldBeLessThan uuid7()
            }
        }

        "multi-threading" {
            val threadPool = Executors.newFixedThreadPool(4)
            repeat(1000) {
                threadPool.submit {
                    println(uuid7())
                }
            }
            threadPool.shutdown()
            threadPool.awaitTermination(100, TimeUnit.SECONDS)
        }
    })
