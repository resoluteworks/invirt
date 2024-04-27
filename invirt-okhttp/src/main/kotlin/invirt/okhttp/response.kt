package invirt.okhttp

import okhttp3.Response
import java.io.File
import java.io.InputStream
import java.io.OutputStream

const val HTTP_OK = 200
const val HTTP_CREATED = 201

fun Response.isOk(): Boolean {
    return code == HTTP_OK || code == HTTP_CREATED
}

fun Response.checkOk(): Response {
    return checkStatus(HTTP_OK, HTTP_CREATED)
}

fun Response.checkStatus(vararg successCodes: Int): Response {
    if (successCodes.isNotEmpty() && !successCodes.contains(code)) {
        throw HttpException(this)
    }
    return this
}

fun Response.text(): String {
    val string = this.body!!.string()
    this.body!!.close()
    return string
}

fun Response.writeTo(file: File): File {
    file.outputStream().use { output ->
        writeTo(output)
    }
    return file
}

fun Response.writeTo(outputStream: OutputStream) {
    this.body!!.byteStream().copyTo(outputStream)
    this.body!!.close()
}

fun <T> Response.withBody(handler: (Long, InputStream) -> T): T {
    val contentLength = this.header("Content-Length")?.toLong() ?: 0
    val result = handler(contentLength, this.body!!.byteStream())
    this.body!!.close()
    return result
}
