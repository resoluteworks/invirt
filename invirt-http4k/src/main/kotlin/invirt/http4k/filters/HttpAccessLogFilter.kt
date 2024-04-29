package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.HttpTransaction
import org.http4k.filter.ResponseFilters

object HttpAccessLogFilter {

    internal val log = KotlinLogging.logger {}

    operator fun invoke(
        errorsOnly: Boolean = true,
        extraFields: (HttpTransaction) -> Map<String, String> = { emptyMap() }
    ): Filter {
        return ResponseFilters.ReportHttpTransaction(recordFn = { tx ->
            if (!errorsOnly || tx.response.status.code >= 400) {
                log.atInfo {
                    message = "http-access"
                    payload = mapOf(
                        "method" to tx.request.method,
                        "uri" to tx.request.uri.toString(),
                        "status" to tx.response.status.code,
                        "durationMs" to tx.duration.toMillis(),
                    ).plus(extraFields(tx))
                }
            }
        })
    }
}
