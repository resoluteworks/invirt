package invirt.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.kotest.shouldHaveStatus

class HotwireTest : StringSpec({

    "turboStream replaces the content type" {
        val response = Response(Status.OK)
            .header("Content-Type", "text/html; charset=utf-8")
            .body("<div>row</div>")
            .turboStream()

        response.headerValues("Content-Type") shouldBe listOf("text/vnd.turbo-stream.html")
        response.bodyString() shouldBe "<div>row</div>"
    }

    "turboStreamRefresh" {
        val response = turboStreamRefresh()

        response shouldHaveStatus Status.OK
        response.header("Content-Type") shouldBe "text/vnd.turbo-stream.html"
        response.bodyString() shouldBe """<turbo-stream action="refresh"></turbo-stream>"""
    }

    "turboStreamRedirect" {
        val response = turboStreamRedirect("/items/42?from=20")

        response shouldHaveStatus Status.OK
        response.header("Content-Type") shouldBe "text/vnd.turbo-stream.html"
        response.bodyString() shouldBe """<turbo-stream action="redirect" target="/items/42?from=20"></turbo-stream>"""
    }
})
