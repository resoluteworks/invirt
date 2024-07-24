package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpTransaction
import org.http4k.core.toParametersMap
import org.http4k.filter.ResponseFilters

object HttpAccessLog {

    // Only lazy to make testing easier
    internal val logger: KLogger by lazy { KotlinLogging.logger {} }

    operator fun invoke(
        allStatues: Boolean = false,
        ignorePaths: Set<String> = emptySet(),
        excludeHeaders: Set<String> = emptySet(),
        extraFields: (HttpTransaction) -> Map<String, String> = { emptyMap() }
    ): Filter = ResponseFilters.ReportHttpTransaction(recordFn = { tx ->
        if ((allStatues || tx.response.status.code >= 400) &&
            ignorePaths.none { tx.request.uri.path.startsWith(it) }
        ) {
            val lowerCaseHeaders = excludeHeaders.map { it.lowercase() }
            logger.atInfo {
                message = "http-access"
                payload = mapOf(
                    "host" to tx.request.header("Host"),
                    "method" to tx.request.method,
                    "path" to tx.request.uri.path,
                    "uri" to tx.request.uri.toString(),
                    "status" to tx.response.status.code,
                    "durationMs" to tx.duration.toMillis(),
                    "headers" to tx.request.headers
                        .filter { it.first.lowercase() !in lowerCaseHeaders }
                        .toParametersMap()
                ).plus(extraFields(tx))
            }
        }
    })
}
