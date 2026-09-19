package invirt.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.UriTemplate
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.RequestWithContext
import org.http4k.routing.routes

class RequestTest : StringSpec({

    // A filter wrapping a whole route group runs before the leaf route binds its path lens, which is the
    // case pathOrNull exists for: http4k attaches the uri template outside the group's filters.
    val handler = Filter { next ->
        { request -> next(request).body(request.pathOrNull("id") ?: "none") }
    }.then(
        routes(
            "/items/{id}" GET { Response(Status.OK) }
        )
    )

    "pathOrNull reads a path variable of the matched route" {
        val response = handler(Request(Method.GET, "/items/42"))

        response shouldHaveStatus Status.OK
        response.bodyString() shouldBe "42"
    }

    "pathOrNull is null for a variable the route does not declare" {
        Request(Method.GET, "/items/42").let { RequestWithContext(it, UriTemplate.from("/items/{id}")) }
            .pathOrNull("openCallId") shouldBe null
    }

    // The branch that matters for a group filter: a path under the group that matches no route carries no
    // uri template at all. http4k's own Request.path throws there, which would turn a 404 into a 500.
    "pathOrNull is null when no route matched the path" {
        val response = handler(Request(Method.GET, "/items/42/extra"))

        response shouldHaveStatus Status.NOT_FOUND
        response.bodyString() shouldBe "none"
    }

    "pathOrNull is null when the path matched but the method did not" {
        val response = handler(Request(Method.POST, "/items/42"))

        response shouldHaveStatus Status.METHOD_NOT_ALLOWED
        response.bodyString() shouldBe "none"
    }

    "pathOrNull is null on a request that was never routed" {
        Request(Method.GET, "/items/42").pathOrNull("id") shouldBe null
    }

    "pathOrNull is null for a value that matched nothing but whitespace" {
        val response = handler(Request(Method.GET, "/items/%20"))

        response.bodyString() shouldBe "none"
    }

    // A filter that rewrites the uri after routing keeps the template of the route that matched, and that
    // template no longer describes the path. Extracting from it would throw, so there is nothing to read.
    "pathOrNull is null when the uri no longer matches the template it was routed with" {
        RequestWithContext(Request(Method.GET, "/items/42"), UriTemplate.from("/items/{id}"))
            .uri(Uri.of("/somewhere/else"))
            .pathOrNull("id") shouldBe null
    }

    "pathWithQuery" {
        Request(Method.GET, "/items/42").pathWithQuery() shouldBe "/items/42"
        Request(Method.GET, "/search?q=blue&size=2").pathWithQuery() shouldBe "/search?q=blue&size=2"
        Request(Method.GET, "/search?").pathWithQuery() shouldBe "/search"
        Request(Method.GET, "https://example.com/items/42?q=blue").pathWithQuery() shouldBe "/items/42?q=blue"
    }
})
