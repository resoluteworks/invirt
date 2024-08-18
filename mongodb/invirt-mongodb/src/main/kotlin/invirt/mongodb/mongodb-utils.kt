package invirt.mongodb

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * MongoDB only support millisecond precision
 */
fun mongoNow(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)
