package invirt.security

import invirt.core.GET
import invirt.core.POST
import invirt.security.authentication.redirectAnonymousToSignIn
import invirt.security.authentication.withPrincipal
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.routes

class RedirectAnonymousToSignInTest : StringSpec({

    "anonymous GET redirects to the URL built from the request" {
        val handler = redirectAnonymousToSignIn { "/sign-in?destination=${it.uri.path}" }
            .then(routes("/dashboard" GET { Response(Status.OK) }))

        val response = handler(Request(Method.GET, "/dashboard"))

        response shouldHaveStatus Status.SEE_OTHER
        response.header("Location") shouldContain "/sign-in?destination=/dashboard"
    }

    "anonymous GET carries the request's path and query into the lambda" {
        val handler = redirectAnonymousToSignIn { "/sign-in?destination=${it.uri.path}?${it.uri.query}" }
            .then(routes("/dashboard" GET { Response(Status.OK) }))

        val response = handler(Request(Method.GET, "/dashboard?tab=published"))

        response.header("Location") shouldContain "/sign-in?destination=/dashboard?tab=published"
    }

    "anonymous Turbo-Frame GET gets a turbo-stream redirect instead of a plain redirect" {
        val handler = redirectAnonymousToSignIn { "/sign-in" }
            .then(routes("/dashboard" GET { Response(Status.OK) }))

        val response = handler(Request(Method.GET, "/dashboard").header("Turbo-Frame", "modal"))

        response shouldHaveStatus Status.OK
        response.header("Content-Type") shouldContain "text/vnd.turbo-stream.html"
        response.bodyString() shouldContain """<turbo-stream action="redirect" target="/sign-in">"""
    }

    "anonymous POST passes through" {
        val handler = redirectAnonymousToSignIn { "/sign-in" }
            .then(routes("/dashboard" POST { Response(Status.ACCEPTED) }))

        val response = handler(Request(Method.POST, "/dashboard"))

        response shouldHaveStatus Status.ACCEPTED
    }

    "authenticated GET passes through" {
        val handler = redirectAnonymousToSignIn { "/sign-in" }
            .then(routes("/dashboard" GET { Response(Status.OK) }))

        val response = handler(Request(Method.GET, "/dashboard").withPrincipal(TestPrincipal()))

        response shouldHaveStatus Status.OK
    }
})
