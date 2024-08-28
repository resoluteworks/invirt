package invirt.http4k

import org.http4k.core.Filter
import org.http4k.filter.CachingFilters
import java.time.Duration

/**
 * A filter that caches the response for the specified number of days.
 */
fun cacheDays(days: Int): Filter = CachingFilters.CacheResponse.MaxAge(Duration.ofDays(days.toLong()))

/**
 * A filter that caches the response for one year.
 */
fun cacheOneYear(): Filter = cacheDays(365)
