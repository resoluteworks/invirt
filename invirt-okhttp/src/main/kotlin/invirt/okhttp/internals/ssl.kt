package invirt.okhttp.internals

import java.security.cert.CertificateException
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = emptyArray()

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
})
