package examples.authentication.service

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * A [Clock] the test moves forward by hand, so session and JWT expiry can be exercised without waiting.
 */
class TestClock(private var now: Instant, private val zone: ZoneId) : Clock() {

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = TestClock(now, zone)

    override fun instant(): Instant = now

    fun plus(duration: Duration) {
        now = now.plus(duration.toJavaDuration())
    }
}
