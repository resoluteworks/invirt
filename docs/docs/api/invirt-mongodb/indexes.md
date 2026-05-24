---
sidebar_position: 5
---

# Indexes

Fluent index builders and helpers for creating them on a collection.

## Index definitions
```kotlin
Product::name.asc()                       // ascending index on "name"
Product::priceMinor.desc()                // descending index on "priceMinor"

"name".asc()                              // by field name
"createdAt".desc()

Product::name.asc { unique() }            // pass IndexOptions configuration
Product::name.asc { caseInsensitive() }   // collation strength TERTIARY, locale "en"

textIndex("title", "description")         // compound text index
```

`caseInsensitive(locale, strength)` is also available as a top-level function returning a
`Collation`, useful for queries:

```kotlin
collection.find(filter).collation(caseInsensitive(locale = "en"))
```

## Creating indexes
```kotlin
collection.createIndices(
    Product::name.asc(),
    Product::priceMinor.desc(),
    *TimestampedDocument.allIndices(),
    textIndex("name", "description")
)
```

`createIndices(vararg)` logs the number of indices created and the time taken.
