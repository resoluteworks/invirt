package invirt.okhttp

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

fun withMockRequest(
    body: String = "",
    code: Int = 200,
    cookie: String? = null,
    block: (MockWebServer, String) -> Unit
) {
    val server = MockWebServer()
    val response = MockResponse()
        .setBody(body)
        .setResponseCode(code)

    if (cookie != null) {
        response.addHeader("Set-Cookie: $cookie")
    }
    server.enqueue(response)
    val baseUrl = "http://${server.hostName}:${server.port}"
    block(server, baseUrl)
}
