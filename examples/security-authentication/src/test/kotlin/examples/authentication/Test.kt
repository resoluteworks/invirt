package examples.authentication

import io.kotest.core.spec.style.StringSpec
import java.time.Instant

class Test : StringSpec({
    "test" {
        println(Instant.now())
        val token = jwtAccessToken("cos@test.com", Instant.now().plusSeconds(2))
        println(token)
        println(verifyJwt(token).subject)
//        Thread.sleep(1100)
//        println(verifyJwt(token).subject)
    }
})
