package invirt.test

import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.with
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.webForm

/**
 * Asserts that the response has a status code of 303 that redirects to the specified [url].
 *
 * @param url The URL to which the response should redirect.
 */
infix fun Response.shouldBeRedirectTo(url: String) {
    this shouldHaveStatus Status.SEE_OTHER
    this.header("Location") shouldBe url
}

/**
 * Creates a POST request with a web form body containing the specified fields.
 *
 * @param uri The URI to which the request will be sent.
 * @param fields A map of field names and values to include in the form body.
 * @return A [Request] object with the specified URI and form body.
 */
fun postForm(uri: String, fields: Map<String, String>): Request {
    val strictFormBody = Body.webForm(Validator.Ignore).toLens()
    return Request(Method.POST, uri)
        .with(strictFormBody of WebForm(fields.map { it.key to listOf(it.value) }.toMap()))
}

/**
 * Asserts that the response contains a cookie, ignoring the expiry field of the cookie.
 * Useful for checking cookies that may have been set with a specific expiry time that
 * is not relevant for the test.
 *
 * @param cookie The [Cookie] object to check for in the response.
 */
infix fun Response.shouldHaveCookieIgnoringExpiry(cookie: Cookie) {
    val responseCookie = this.cookies().find { it.name == cookie.name }
    responseCookie shouldNotBe null
    responseCookie!!.shouldBeEqualToIgnoringFields(cookie, Cookie::expires)
}
