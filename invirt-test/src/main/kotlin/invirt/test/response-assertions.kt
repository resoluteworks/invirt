package invirt.test

import invirt.core.views.ErrorViewModel
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.validk.ValidationErrors
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
 * Asserts that the response contains a view model of the expected type [V].
 *
 * @param V The expected type of the view model.
 */
inline fun <reified V : ViewModel> Response.shouldHaveViewModel(): V {
    val viewModel = ResponseKey.of<ViewModel>("viewModel")(this)
    return viewModel.shouldBeInstanceOf<V>()
}

/**
 * Asserts that the response contains an [ErrorViewModel] with a non-null model of the expected type [V]
 * and returns a pair of the model and the validation errors.
 *
 * @param V The expected type of the model within the [ErrorViewModel].
 * @return A pair containing the model of type [V] and the associated [ValidationErrors].
 */
inline fun <reified V : ViewModel> Response.shouldBeErrorResponse(): Pair<V, ValidationErrors> {
    val viewModel = ResponseKey.of<ViewModel>("viewModel")(this)
    viewModel.shouldBeInstanceOf<ErrorViewModel>()
    viewModel.model shouldNotBe null
    viewModel.model.shouldBeInstanceOf<V>()
    return Pair(viewModel.model as V, viewModel.errors)
}

/**
 * Asserts that the response contains an [ErrorViewModel] with a null model
 * and returns the associated validation errors.
 *
 * @return The [ValidationErrors] associated with the [ErrorViewModel].
 */
fun Response.shouldBeErrorResponse(): ValidationErrors {
    val viewModel = ResponseKey.of<ViewModel>("viewModel")(this)
    viewModel.shouldBeInstanceOf<ErrorViewModel>()
    viewModel.model shouldBe null
    return viewModel.errors
}
