package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.filter.ResponseFilters

object HttpAccessLogFilter {

    internal val log = KotlinLogging.logger {}

    operator fun invoke(errorsOnly: Boolean = true): Filter {
        return ResponseFilters.ReportHttpTransaction(recordFn = { tx ->
            if (!errorsOnly || tx.response.status.code >= 400) {
                log.atInfo {
                    message = "http-access"
                    payload = mapOf(
                        "method" to tx.request.method,
                        "uri" to tx.request.uri,
                        "status" to tx.response.status,
                        "durationMs" to tx.duration.toMillis()
                    )
                }
            }
        })
    }
}
