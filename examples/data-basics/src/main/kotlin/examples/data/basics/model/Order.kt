package examples.data.basics.model

import invirt.utils.uuid7
import java.time.Instant

data class Order(
    val status: OrderStatus,
    val totalMinorUnit: Long,
    val createdAt: Instant,
    val id: String = uuid7()
)

enum class OrderStatus(val label: String) {
    PROCESSING("Processing"),
    DISPATCHED("Dispatched"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Delivered")
}
