package invirt.mongodb

import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Returns the current time as an [Instant] truncated to milliseconds.
 * This is the default timestamp format used in MongoDB.
 */
fun mongoNow(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)
