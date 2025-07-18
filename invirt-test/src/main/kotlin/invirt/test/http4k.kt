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
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.http4k.core.with
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.webForm

infix fun Response.shouldBeRedirectTo(url: String) {
    this shouldHaveStatus Status.SEE_OTHER
    this.header("Location") shouldBe url
}

fun postForm(uri: String, fields: Map<String, String>): Request {
    val strictFormBody = Body.webForm(Validator.Ignore).toLens()
    return Request(Method.POST, uri)
        .with(strictFormBody of WebForm(fields.map { it.key to listOf(it.value) }.toMap()))
}

infix fun Response.shouldHaveCookieIgnoringExpiry(cookie: Cookie) {
    val responseCookie = this.cookies().find { it.name == cookie.name }
    responseCookie shouldNotBe null
    responseCookie!!.shouldBeEqualToIgnoringFields(cookie, Cookie::expires)
}
