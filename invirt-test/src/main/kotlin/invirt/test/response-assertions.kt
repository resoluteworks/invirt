package invirt.test

import invirt.core.views.InvirtRenderModel
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
 * Asserts that the response contains a model of the expected type [M].
 */
inline fun <reified M : Any> Response.shouldHaveModel(): M {
    val renderModal = ResponseKey.of<InvirtRenderModel>("invirtRenderModel")(this)
    return renderModal.model.shouldBeInstanceOf<M>()
}

/**
 * Asserts that the response is rendered with the specified [template].
 */
infix fun Response.shouldHaveTemplate(template: String) {
    val renderModal = ResponseKey.of<InvirtRenderModel>("invirtRenderModel")(this)
    renderModal.template shouldBe template
}

/**
 * Asserts that the response contains a non-null model of the expected type [M] and validation errors,
 * and returns a pair of the model and the validation errors.
 */
inline fun <reified M : Any> Response.shouldBeErrorResponse(): Pair<M, ValidationErrors> {
    val invirtRenderModel = ResponseKey.of<InvirtRenderModel>("invirtRenderModel")(this)
    invirtRenderModel.model shouldNotBe null
    invirtRenderModel.model.shouldBeInstanceOf<M>()
    invirtRenderModel.errors shouldNotBe null
    return Pair(invirtRenderModel.model as M, invirtRenderModel.errors!!)
}

/**
 * Asserts that the response contains validation errors and returns them.
 */
fun Response.shouldBeErrorResponse(): ValidationErrors {
    val invirtRenderModel = ResponseKey.of<InvirtRenderModel>("invirtRenderModel")(this)
    invirtRenderModel.errors shouldNotBe null
    return invirtRenderModel.errors!!
}
