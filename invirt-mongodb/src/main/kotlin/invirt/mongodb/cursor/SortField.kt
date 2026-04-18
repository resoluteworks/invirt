package invirt.mongodb.cursor

import invirt.data.Sort

data class SortField<Doc>(
    val sort: Sort,
    val extractor: (Doc) -> Any
)
