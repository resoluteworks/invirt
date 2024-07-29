package invirt.http4k

import org.http4k.core.Filter
import org.http4k.filter.CachingFilters
import java.time.Duration

fun cacheDays(days: Int): Filter = CachingFilters.CacheResponse.MaxAge(Duration.ofDays(days.toLong()))

fun cacheOneYear(): Filter = cacheDays(365)
