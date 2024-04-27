package invirt.okhttp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.util.*

class ClientTest : StringSpec({

    "follows redirects" {
        val response = newHttpClient().get("https://the-internet.herokuapp.com/redirect")
        response.code shouldBe 200
        response.text() shouldContain "HTTP status codes are a standard set of numbers used to communicate"
    }

    "don't follows redirects" {
        val response = newHttpClient { noRedirects() }.get("https://the-internet.herokuapp.com/redirect")
        response.code shouldBe 302
    }

    "HEAD" {
        withMockRequest { server, url ->
            newHttpClient().head(url)
            server.takeRequest().method shouldBe "HEAD"
        }
    }

    "HEAD with headers" {
        withMockRequest { server, url ->
            val headerValue = UUID.randomUUID().toString()
            newHttpClient().head(url, mapOf("header1" to headerValue))
            val request = server.takeRequest()
            request.method shouldBe "HEAD"
            request.headers["header1"] shouldBe headerValue
        }
    }

    "GET with headers" {
        withMockRequest { server, url ->
            val headerValue = UUID.randomUUID().toString()
            newHttpClient().get(url, mapOf("myheader" to headerValue))
            server.takeRequest().headers["myheader"] shouldBe headerValue
        }
    }

    "POST" {
        withMockRequest { server, url ->
            val body = """{"name": "john smith"}"""
            newHttpClient().post(url, body, "application/json")
            val request = server.takeRequest()
            request.headers["Content-Type"] shouldBe "application/json; charset=utf-8"
            request.body.readUtf8() shouldBe body
        }
    }

    "POST form" {
        withMockRequest { server, url ->
            newHttpClient().postForm(url, mapOf("name" to "John Smith", "age" to 10))
            val request = server.takeRequest()
            request.headers["Content-Type"] shouldBe "application/x-www-form-urlencoded"
            request.body.readUtf8() shouldBe "name=John%20Smith&age=10"
        }
    }

    "POST form with headers" {
        withMockRequest { server, url ->
            val headerValue = UUID.randomUUID().toString()
            newHttpClient().postForm(url, mapOf("name" to "John Smith", "age" to 10), mapOf("myheader" to headerValue))
            server.takeRequest().headers["myheader"] shouldBe headerValue
        }
    }
})
