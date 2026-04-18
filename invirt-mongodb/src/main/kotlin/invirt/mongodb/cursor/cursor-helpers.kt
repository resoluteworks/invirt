package invirt.mongodb.cursor

import com.mongodb.client.model.Aggregates
import com.mongodb.kotlin.client.MongoCollection
import org.bson.conversions.Bson

/**
 * Generates a CursorPage for the current document, including next and previous
 * cursor tokens if applicable.
 *
 * @param current The document for which we want the cursor page
 * @param basePipeline The pipeline that would be used for this document in a cursorAggregate query. This is used to check for the existence of next/prev documents.
 * @param sortFields The fields that define the sort order for the cursor. Must be consistent with the sort used in the basePipeline.
 * @return A [CursorPage] containing the current document and next/prev cursor tokens if applicable.
 */
fun <Doc : Any> MongoCollection<Doc>.cursorPageForCurrent(
    current: Doc,
    basePipeline: List<Bson>,
    sortFields: List<CursorSortField<Doc>>
): CursorPage<Doc> {
    require(sortFields.isNotEmpty()) { "sortFields must not be empty" }

    val sort = sortFields.map { it.sort }

    fun buildCursor(direction: CursorDirection): Cursor =
        Cursor(
            sort = sort,
            values = sortFields.map { CursorValue.of(it.extractor(current)) },
            direction = direction
        )

    val prevCursor = buildCursor(CursorDirection.BACKWARD)
    val nextCursor = buildCursor(CursorDirection.FORWARD)

    val hasPrev = aggregate(
        basePipeline + listOf(
            Aggregates.match(prevCursor.toFilter()),
            Aggregates.limit(1)
        )
    ).firstOrNull() != null

    val hasNext = aggregate(
        basePipeline + listOf(
            Aggregates.match(nextCursor.toFilter()),
            Aggregates.limit(1)
        )
    ).firstOrNull() != null

    return CursorPage(
        data = listOf(current),
        prevCursor = if (hasPrev) prevCursor.token else null,
        nextCursor = if (hasNext) nextCursor.token else null
    )
}
