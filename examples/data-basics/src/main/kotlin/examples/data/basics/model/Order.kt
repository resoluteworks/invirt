package examples.data.basics.model

import invirt.utils.uuid7
import java.time.Instant

data class Order(
    val status: OrderStatus,
    val total: Double,
    val createdAt: Instant,
    val id: String = uuid7()
)

enum class OrderStatus {
    PROCESSING,
    DISPATCHED,
    IN_TRANSIT,
    DELIVERED
}
