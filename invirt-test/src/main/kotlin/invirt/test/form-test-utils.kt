package invirt.test

import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.webForm

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
