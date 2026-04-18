package invirt.mongodb.cursor

data class CursorPage<Doc>(
    val data: List<Doc>,
    val nextCursor: String?,
    val prevCursor: String?
)
