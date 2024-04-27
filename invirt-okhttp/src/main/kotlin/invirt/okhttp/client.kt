package invirt.okhttp

import invirt.okhttp.internals.trustAllCerts
import okhttp3.FormBody
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.noRedirects(): OkHttpClient.Builder {
    followRedirects(false)
    return this
}

fun OkHttpClient.Builder.ignoreSslErrors(): OkHttpClient.Builder {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())
    val sslSocketFactory = sslContext.socketFactory
    sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}

fun OkHttpClient.Builder.baseUrl(baseUrl: String): OkHttpClient.Builder {
    addInterceptor { chain ->
        val request = chain.request()
        val newRequest = request.newBuilder().url(baseUrl + request.url).build()
        chain.proceed(newRequest)
    }
    return this
}

fun newHttpClient(init: OkHttpClient.Builder.() -> Unit = { }): OkHttpClient {
    val builder = OkHttpClient().newBuilder()
    init(builder)
    return builder.build()
}

fun OkHttpClient.get(url: String, headers: Map<String, String> = emptyMap()): Response {
    val request = Request.Builder()
        .url(url)
        .get()
        .headers(headers.toHeaders())
        .build()
    return newCall(request).execute()
}

fun OkHttpClient.head(url: String, headers: Map<String, String> = emptyMap()): Response {
    val request = Request.Builder()
        .url(url)
        .head()
        .headers(headers.toHeaders())
        .build()
    return newCall(request).execute()
}

fun OkHttpClient.post(
    url: String,
    data: String,
    mediaType: String,
    headers: Map<String, String> = emptyMap()
): Response {
    val request = Request.Builder()
        .url(url)
        .post(data.toRequestBody(mediaType.toMediaType()))
        .headers(headers.toHeaders())
        .build()
    return newCall(request).execute()
}

fun OkHttpClient.postForm(
    url: String,
    data: Map<String, Any>,
    headers: Map<String, String> = emptyMap()
): Response {
    val formBody = FormBody.Builder()
    data.forEach { (key, value) -> formBody.add(key, value.toString()) }
    val request = Request.Builder()
        .url(url)
        .post(formBody.build())
        .headers(headers.toHeaders())
        .build()
    return newCall(request).execute()
}
