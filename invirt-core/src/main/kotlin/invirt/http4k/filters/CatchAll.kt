package invirt.http4k.filters

import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import kotlin.reflect.KClass

private val log = KotlinLogging.logger {}

object CatchAll {

    operator fun invoke(vararg exceptionStatusMappings: Pair<KClass<out Throwable>, Status>): Filter {
        return invoke(exceptionStatusMappings.toMap())
    }

    operator fun invoke(exceptionStatusMappings: Map<KClass<out Throwable>, Status>): Filter = Filter { next ->
        { request ->
            try {
                next(request)
            } catch (t: Throwable) {
                log.error(t) { t.message }
                exceptionStatusMappings[t::class]
                    ?.let { Response(it) }
                    ?: Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
