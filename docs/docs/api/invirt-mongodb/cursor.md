---
sidebar_position: 7
---

# Cursor pagination

For pipelines where offset-based pagination is impractical (large result sets, frequent inserts), the
cursor helpers provide stable forward / backward pagination using opaque base64-encoded tokens.

```kotlin
fun <Doc : Any> MongoCollection<Doc>.cursorAggregate(
    basePipeline: List<Bson>,
    limit: Int,
    currentCursorToken: String?,
    sortFields: List<CursorSortField<Doc>>
): CursorPage<Doc>

fun <Doc : Any> MongoCollection<Doc>.cursorPageForCurrent(
    current: Doc,
    basePipeline: List<Bson>,
    sortFields: List<CursorSortField<Doc>>
): CursorPage<Doc>

data class CursorPage<Doc>(
    val data: List<Doc>,
    val nextCursor: String?,
    val prevCursor: String?
)

data class CursorSortField<Doc>(
    val sort: Sort,
    val extractor: (Doc) -> Any
)
```

### Mechanics
* `sortFields` describes the sort key the cursor follows. The order of fields matters: the cursor
  uses the same ordering to break ties.
* `extractor` returns the value of the corresponding field on a document &mdash; used when serialising
  the cursor for the document at the page boundary.
* Supported field types are `String`, `Instant`, `LocalDate`, `Long`, `Int`, `Double`.
* `cursorAggregate` returns `nextCursor` / `prevCursor` if there are more documents in the respective
  direction.
* `cursorPageForCurrent` builds a single-document page around `current`, including the prev/next
  tokens that would navigate from it. Useful when rendering a "detail" view that needs neighbour
  navigation.

### Example
```kotlin
val sortFields = listOf(
    CursorSortField(Sort.desc("createdAt"), Order::createdAt),
    CursorSortField(Sort.asc("_id"), Order::id)
)

val page = orderCollection.cursorAggregate(
    basePipeline = listOf(Aggregates.match(filter)),
    limit = 20,
    currentCursorToken = request.query("cursor"),
    sortFields = sortFields
)

// page.data, page.nextCursor, page.prevCursor
```
