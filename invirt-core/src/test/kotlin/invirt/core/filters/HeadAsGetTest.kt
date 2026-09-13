package invirt.core.filters

import invirt.core.GET
import invirt.core.POST
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class HeadAsGetTest : StringSpec({

    "head on a get-only route returns the get response with an empty body" {
        var methodSeenByHandler: Method? = null
        val httpHandler = HeadAsGet(
            routes(
                "/test" GET { request ->
                    methodSeenByHandler = request.method
                    Response(Status.OK)
                        .header("X-Custom", "custom-value")
                        .header("Content-Type", "application/json")
                        .body("""{"ok":true}""")
                }
            )
        )

        val response = httpHandler(Request(Method.HEAD, "/test"))

        response shouldHaveStatus Status.OK
        response.header("X-Custom") shouldBe "custom-value"
        response.header("Content-Type") shouldBe "application/json"
        response.bodyString() shouldBe ""
        methodSeenByHandler shouldBe Method.GET
    }

    "get on a get route is unchanged" {
        val httpHandler = HeadAsGet(
            routes(
                "/test" GET { Response(Status.OK).body("hello") }
            )
        )

        val response = httpHandler(Request(Method.GET, "/test"))

        response shouldHaveStatus Status.OK
        response.bodyString() shouldBe "hello"
    }

    "post on a post route is unchanged" {
        var methodSeenByHandler: Method? = null
        val httpHandler = HeadAsGet(
            routes(
                "/test" POST { request ->
                    methodSeenByHandler = request.method
                    Response(Status.CREATED).body("created")
                }
            )
        )

        val response = httpHandler(Request(Method.POST, "/test"))

        response shouldHaveStatus Status.CREATED
        response.bodyString() shouldBe "created"
        methodSeenByHandler shouldBe Method.POST
    }

    "head on a post-only route is still not allowed" {
        val httpHandler = HeadAsGet(
            routes(
                "/test" POST { Response(Status.CREATED) }
            )
        )

        httpHandler(Request(Method.HEAD, "/test")) shouldHaveStatus Status.METHOD_NOT_ALLOWED
    }

    "head on an unknown path is not found" {
        val httpHandler = HeadAsGet(
            routes(
                "/test" GET { Response(Status.OK).body("hello") }
            )
        )

        val response = httpHandler(Request(Method.HEAD, "/unknown"))

        response shouldHaveStatus Status.NOT_FOUND
        response.bodyString() shouldBe ""
    }

    "a filter composed inside gets its body dropped, a filter composed outside sees the original method" {
        var methodSeenOutside: Method? = null
        val outside = Filter { next ->
            { request ->
                methodSeenOutside = request.method
                next(request)
            }
        }
        val errorPage = Filter { next ->
            { request ->
                val response = next(request)
                if (response.status == Status.NOT_FOUND) response.body("<h1>Not found</h1>") else response
            }
        }
        val httpHandler = outside.then(
            HeadAsGet(
                errorPage.then(
                    routes(
                        "/test" GET { Response(Status.OK).body("hello") }
                    )
                )
            )
        )

        val response = httpHandler(Request(Method.HEAD, "/unknown"))

        response shouldHaveStatus Status.NOT_FOUND
        response.bodyString() shouldBe ""
        methodSeenOutside shouldBe Method.HEAD
        httpHandler(Request(Method.GET, "/unknown")).bodyString() shouldBe "<h1>Not found</h1>"
    }
})
