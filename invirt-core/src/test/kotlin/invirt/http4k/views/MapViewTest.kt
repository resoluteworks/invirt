package invirt.http4k.views

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.bind
import org.http4k.routing.routes

class MapViewTest : StringSpec({

    "MapView" {
        val httpHandler = routes("/test" bind Method.GET to { MapView(mapOf("key" to "value"), "map-view").ok() })
        val response = httpHandler(Request(Method.GET, "/test"))
        response.bodyString().trim() shouldBe "value"
    }

    "Map.withView" {
        val httpHandler = routes("/test" bind Method.GET to { mapOf("key" to "value").withView("map-view") })
        val response = httpHandler(Request(Method.GET, "/test"))
        response.bodyString().trim() shouldBe "value"
    }

    "Pair.withView" {
        val httpHandler = routes("/test" bind Method.GET to { ("key" to "value").withView("map-view") })
        val response = httpHandler(Request(Method.GET, "/test"))
        response.bodyString().trim() shouldBe "value"
    }
})
