package invirt.data

import kotlin.reflect.KProperty

data class Sort(
    val field: String,
    val order: SortOrder
) {

    fun revert(): Sort {
        return Sort(field, order.revert())
    }

    override fun toString(): String {
        return "$field:$order"
    }

    companion object {
        operator fun invoke(sortString: String): Sort {
            val elements = sortString.split(":")
            if (elements.size != 2 || elements.any { it.isBlank() }) {
                throw IllegalArgumentException("Invalid sort string $sortString")
            }
            val orderStr = elements[1].uppercase()
            val order = enumValues<SortOrder>().firstOrNull { it.name == orderStr }
            if (order == null) {
                throw IllegalArgumentException("Invalid sort order $orderStr")
            }
            return Sort(elements[0], order)
        }

        fun asc(field: String) = Sort(field, SortOrder.ASC)
        fun desc(field: String) = Sort(field, SortOrder.DESC)
    }
}

enum class SortOrder {
    ASC,
    DESC;

    fun revert(): SortOrder = if (this == ASC) DESC else ASC
}

fun KProperty<*>.sortAsc(): Sort {
    return Sort(this.name, SortOrder.ASC)
}

fun KProperty<*>.sortDesc(): Sort {
    return Sort(this.name, SortOrder.DESC)
}
