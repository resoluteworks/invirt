package invirt.mongodb.cursor

import java.time.Instant
import java.time.LocalDate

internal data class CursorValue(val type: Type, val value: String) {

    enum class Type {
        STRING,
        INSTANT,
        LOCAL_DATE,
        LONG,
        INT,
        DOUBLE
    }

    fun decode(): Any = when (type) {
        Type.STRING -> value
        Type.INSTANT -> Instant.parse(value)
        Type.LOCAL_DATE -> LocalDate.parse(value)
        Type.LONG -> value.toLong()
        Type.INT -> value.toInt()
        Type.DOUBLE -> value.toDouble()
    }

    companion object {
        fun of(value: Any): CursorValue = when (value) {
            is String -> CursorValue(Type.STRING, value)
            is Instant -> CursorValue(Type.INSTANT, value.toString())
            is LocalDate -> CursorValue(Type.LOCAL_DATE, value.toString())
            is Long -> CursorValue(Type.LONG, value.toString())
            is Int -> CursorValue(Type.INT, value.toString())
            is Double -> CursorValue(Type.DOUBLE, value.toString())
            else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
        }
    }
}
