package invirt.mongodb.cursor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mongodb.client.model.Filters
import invirt.data.Sort
import org.bson.conversions.Bson
import java.util.Base64

internal data class Cursor(
    val sort: List<Sort>,
    val values: List<CursorValue>,
    val direction: CursorDirection
) {

    @get:JsonIgnore
    val token: String by lazy { Base64.getUrlEncoder().encodeToString(jsonMapper.writeValueAsBytes(this)) }

    fun toFilter(): Bson {
        require(sort.size == values.size)

        val decoded = values.map { it.decode() }
        val conditions = mutableListOf<Bson>()

        for (i in sort.indices) {
            val andConditions = mutableListOf<Bson>()

            for (j in 0 until i) {
                andConditions += sort[j].eq(decoded[j])
            }

            val field = sort[i]
            val value = decoded[i]

            val comparison = when (direction) {
                CursorDirection.FORWARD -> field.gt(value)
                CursorDirection.BACKWARD -> field.lt(value)
            }

            andConditions += comparison

            conditions += Filters.and(andConditions)
        }

        return Filters.or(conditions)
    }

    companion object {
        fun fromToken(token: String): Cursor {
            val decoded = Base64.getUrlDecoder().decode(token)
            return jsonMapper.readValue(decoded, Cursor::class.java)
        }
    }
}

internal enum class CursorDirection {
    FORWARD,
    BACKWARD
}
