package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpTransaction
import org.http4k.filter.ResponseFilters

object HttpAccessLog {

    internal val log = KotlinLogging.logger {}

    operator fun invoke(
        allStatues: Boolean = false,
        ignorePaths: Set<String> = emptySet(),
        extraFields: (HttpTransaction) -> Map<String, String> = { emptyMap() }
    ): Filter {
        return ResponseFilters.ReportHttpTransaction(recordFn = { tx ->
            if ((allStatues || tx.response.status.code >= 400) && ignorePaths.none { tx.request.uri.path.startsWith(it) }) {
                log.atInfo {
                    message = "http-access"
                    payload = mapOf(
                        "host" to tx.request.header("Host"),
                        "method" to tx.request.method,
                        "path" to tx.request.uri.path,
                        "uri" to tx.request.uri.toString(),
                        "status" to tx.response.status.code,
                        "durationMs" to tx.duration.toMillis()
                    ).plus(extraFields(tx))
                }
            }
        })
    }
}
