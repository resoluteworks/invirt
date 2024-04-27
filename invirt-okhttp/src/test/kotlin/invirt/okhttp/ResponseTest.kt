package invirt.okhttp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class ResponseTest : StringSpec({

    "checkOk" {
        val response = newHttpClient { noRedirects() }.get("https://the-internet.herokuapp.com/redirect")
        shouldThrow<HttpException> {
            response.checkOk()
        }
    }

    "writeTo File" {
        val file = File(UUID.randomUUID().toString())
        file.deleteOnExit()
        val response = "This is the response " + UUID.randomUUID().toString()
        withMockRequest(body = response) { server, url ->
            newHttpClient().get(url).writeTo(file)
            file.readText() shouldBe response
        }
    }

    "writeTo OutputStream" {
        val response = "This is the response " + UUID.randomUUID().toString()
        withMockRequest(body = response) { server, url ->
            val outputStream = ByteArrayOutputStream()
            newHttpClient().get(url).writeTo(outputStream)
            String(outputStream.toByteArray()) shouldBe response
        }
    }

    "withBody" {
        val response = "This is the response " + UUID.randomUUID().toString()
        withMockRequest(body = response) { server, url ->
            newHttpClient().get(url).withBody { size, inputStream ->
                size shouldBe response.length
                String(inputStream.readAllBytes()) shouldBe response
            }
        }
    }
})
