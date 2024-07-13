package invirt.http4k

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes

class JsonTest : StringSpec() {

    init {
        "json" {
            data class JsonTestPojo(
                val name: String,
                val enabled: Boolean
            )

            val httpHandler = routes(
                "/test" bind Method.GET to {
                    val lens = jsonLens<JsonTestPojo>()
                    Response(Status.OK).with(lens of JsonTestPojo("John Smith", false))
                }
            )

            httpHandler(Request(Method.GET, "/test")).bodyString() shouldBe """{"name":"John Smith","enabled":false}"""
        }
    }
}
