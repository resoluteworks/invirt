package invirt.mongodb.cursor

import invirt.data.Sort

data class CursorSortField<Doc>(
    val sort: Sort,
    val extractor: (Doc) -> Any
)
