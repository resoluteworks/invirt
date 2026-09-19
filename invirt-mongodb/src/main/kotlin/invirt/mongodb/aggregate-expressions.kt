package invirt.mongodb

import org.bson.Document

/**
 * The expression that reads a scalar out of the array a `$lookup` leaves behind, for a lookup whose
 * sub-pipeline resolves to at most one document.
 *
 * [lookupField] is the `as` field of the `$lookup`, [path] the field to read inside the looked-up
 * document, and [default] the value to fall back to when the lookup matched nothing. A `$lookup` that
 * matches nothing produces an empty array, and `$arrayElemAt` over it resolves to missing, so a
 * non-null [default] is what turns that into a value the document can carry.
 *
 * ```kotlin
 * // $ifNull: [ { $arrayElemAt: [ "$organisationLookup.name", 0 ] }, "Unknown organisation" ]
 * Field("organisationName", mongoLookupField("organisationLookup", "name", "Unknown organisation"))
 *
 * // $arrayElemAt: [ "$allocationLookup", 0 ]
 * Field("allocation", mongoLookupField("allocationLookup"))
 * ```
 *
 * The `$ifNull` wrapper is added only when [default] is not null. Without it the expression resolves
 * to missing on an empty lookup, which is what a caller comparing the raw element wants: the field
 * is simply absent rather than defaulted to something the comparison would have to know about.
 */
fun mongoLookupField(lookupField: String, path: String? = null, default: Any? = null): Document {
    val fieldPath = if (path == null) "\$$lookupField" else "\$$lookupField.$path"
    val firstElement = Document("\$arrayElemAt", listOf(fieldPath, 0))
    return if (default == null) {
        firstElement
    } else {
        Document("\$ifNull", listOf(firstElement, default))
    }
}

/**
 * The expression that matches [field] of the document being looked up against the `let` variable
 * [letVariable] of the correlated `$lookup` running the sub-pipeline, i.e.
 * `{ $eq: [ "$field", "$$letVariable" ] }`.
 *
 * It is an expression rather than a filter, so a `$match` stage wraps it in `Filters.expr` and a
 * larger expression uses it as it is:
 *
 * ```kotlin
 * Aggregates.match(Filters.expr(mongoCorrelatedEq(ConsentEvent::userId.name, "consentUserId")))
 * ```
 */
fun mongoCorrelatedEq(field: String, letVariable: String): Document =
    Document("\$eq", listOf("\$$field", "\$\$$letVariable"))
