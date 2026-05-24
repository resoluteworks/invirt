---
sidebar_position: 4
---

# Filters and sorts

`Bson` filter and sort builders derived from `KProperty` references or strings, plus the conversion
from Invirt's [`DataFilter`](/docs/api/invirt-data/data-filter) / [`Sort`](/docs/api/invirt-data/sort)
abstractions.

## Filters by id
```kotlin
mongoById("123")              // Filters.eq("_id", "123")
mongoByIds(listOf("a", "b"))  // Filters.in("_id", ...)
mongoByIds("a", "b")
```

## Field filters via KProperty
```kotlin
Product::priceMinor.mongoEq(1000_00)
Product::priceMinor.mongoGt(1000_00)
Product::priceMinor.mongoGte(1000_00)
Product::priceMinor.mongoLt(1000_00)
Product::priceMinor.mongoLte(1000_00)
Product::tags.mongoIn("red", "green")
Product::tags.mongoIn(listOf("red", "green"))
Product::email.mongoExists()
```

The same operations are available as String extensions (`"priceMinor".mongoGt(1000_00)` etc).

## Date helpers
```kotlin
Order::date.inYear(2026)   // and(>= 2026-01-01, <= 2026-12-31)
```

## Geo filters
```kotlin
Location::point.mongoGeoBounds(boundingBox)
"point".mongoGeoBounds(boundingBox)
```

Internally builds a `Filters.geoWithin` polygon from the
[`GeoBoundingBox`](/docs/api/invirt-data/geo#geoboundingbox) corners.

## Text search
```kotlin
mongoTextSearch("invirt")   // Filters.text("invirt")
```

## Null-aware combinators
`mongoAnd` and `mongoOr` accept nullable filters and skip the nulls, returning `Filters.empty()` when
the resulting list is empty. Convenient for building filters from optional inputs.

```kotlin
val filter = mongoAnd(
    minPrice?.let { Product::priceMinor.mongoGte(it) },
    maxPrice?.let { Product::priceMinor.mongoLte(it) },
    onlyAvailable.takeIf { it }?.let { Product::available.mongoEq(true) }
)
```

## Bridge from invirt-data
Translate Invirt's database-agnostic constructs into MongoDB:

```kotlin
val mongoFilter: Bson = invirtFilter.mongoFilter()
val mongoSort: Bson   = Sort.desc("createdAt").mongoSort()
val mongoSorts: List<Bson> = listOf(Sort.desc("createdAt")).mongoSort()

findIterable.sort(Sort.desc("createdAt"))
findIterable.sort(listOf(Sort.asc("name"), Sort.desc("createdAt")))
findIterable.page(Page(0, 20))   // .skip().limit()
```

## Sort by KProperty
```kotlin
Product::priceMinor.mongoSortAsc()
Product::priceMinor.mongoSortDesc()
"priceMinor".mongoSortAsc()
"priceMinor".mongoSortDesc()
```
