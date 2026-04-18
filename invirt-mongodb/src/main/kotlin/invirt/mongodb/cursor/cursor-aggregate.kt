package invirt.mongodb.cursor

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Aggregates.match
import com.mongodb.kotlin.client.MongoCollection
import org.bson.conversions.Bson

/**
 * Runs an aggregation pipeline using a cursor pagination mechanism.
 * The basePipeline should produce documents that can be sorted by the provided sortFields.
 *
 * @param basePipeline The base pipeline
 * @param limit The number of documents to return
 * @param currentCursorToken The cursor token from the client, or null for the first page
 * @param sortFields The fields to sort by, in order of precedence. Must be consistent with the cursor values.
 * @return A CursorPage containing the page of results and next/prev cursor tokens if applicable.
 */
fun <Doc : Any> MongoCollection<Doc>.cursorAggregate(
    basePipeline: List<Bson>,
    limit: Int,
    currentCursorToken: String?,
    sortFields: List<SortField<Doc>>
): CursorPage<Doc> {
    require(limit > 0) { "limit must be > 0" }
    require(sortFields.isNotEmpty()) { "sortFields must not be empty" }

    val sort = sortFields.map { it.sort }
    val cursor = currentCursorToken?.let { Cursor.fromToken(it) }

    if (cursor != null) {
        require(cursor.sort == sort) {
            "Cursor sort does not match query sort"
        }
        require(cursor.values.size == sort.size) {
            "Cursor values do not match sort size"
        }
    }

    val direction = cursor?.direction ?: CursorDirection.FORWARD
    val forwardSort = sort.forwardSort()
    val reverseSort = sort.reverseSort()
    val querySort = when (direction) {
        CursorDirection.FORWARD -> forwardSort
        CursorDirection.BACKWARD -> reverseSort
    }

    fun buildCursor(doc: Doc, direction: CursorDirection): Cursor =
        Cursor(
            sort = sort,
            values = sortFields.map { CursorValue.of(it.extractor(doc)) },
            direction = direction
        )

    // -------------------------
    // 1. MAIN PAGE QUERY
    // -------------------------
    val pipeline = mutableListOf<Bson>().apply {
        addAll(basePipeline)

        if (cursor != null) {
            add(match(cursor.toFilter()))
        }

        add(Aggregates.sort(querySort))
        add(Aggregates.limit(limit + 1))
    }

    val rawResults = aggregate(pipeline).toList()

    if (rawResults.isEmpty()) {
        return CursorPage(emptyList(), null, null)
    }

    val hasMoreInQueryDirection = rawResults.size > limit
    val trimmed = if (hasMoreInQueryDirection) rawResults.dropLast(1) else rawResults

    val pageData = when (direction) {
        CursorDirection.FORWARD -> trimmed
        CursorDirection.BACKWARD -> trimmed.reversed()
    }

    val first = pageData.first()
    val last = pageData.last()

    val firstBackwardCursor = buildCursor(first, CursorDirection.BACKWARD)
    val lastForwardCursor = buildCursor(last, CursorDirection.FORWARD)

    // -------------------------
    // 2. OPPOSITE-SIDE EXISTENCE
    // -------------------------
    val hasPrev: Boolean
    val hasNext: Boolean

    when (direction) {
        CursorDirection.FORWARD -> {
            hasNext = hasMoreInQueryDirection
            hasPrev = if (currentCursorToken == null) {
                false
            } else {
                aggregate(
                    basePipeline + listOf(
                        match(firstBackwardCursor.toFilter()),
                        Aggregates.limit(1)
                    )
                ).firstOrNull() != null
            }
        }

        CursorDirection.BACKWARD -> {
            hasPrev = hasMoreInQueryDirection
            hasNext = aggregate(
                basePipeline + listOf(
                    match(lastForwardCursor.toFilter()),
                    Aggregates.limit(1)
                )
            ).firstOrNull() != null
        }
    }

    return CursorPage(
        data = pageData,
        nextCursor = if (hasNext) lastForwardCursor.token else null,
        prevCursor = if (hasPrev) firstBackwardCursor.token else null
    )
}
