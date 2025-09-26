package invirt.test

import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookies
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.ResponseKey
import org.http4k.template.ViewModel

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

/**
 * Asserts that the response is an instance of [InvirtResponse] and that its view model
 * is of the specified type [V].
 *
 * @param V The expected type of the view model.
 */
inline fun <reified V : ViewModel> Response.shouldBeViewModel(): V {
    val viewModel = ResponseKey.of<ViewModel>("viewModel")(this)
    return viewModel.shouldBeInstanceOf<V>()
}
