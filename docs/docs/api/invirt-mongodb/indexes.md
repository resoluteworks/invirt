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

## Compound indexes
`ascIndex(vararg)` builds an index over several fields running in the same direction, in the order
given. A single field is the same index `asc()` builds:

```kotlin
ascIndex(Run::organisationId, Run::descriptionHash, Run::status)
ascIndex("nested.one", "nested.two")                        // by field name

ascIndex(Run::organisationId, Run::draftId) {               // options apply to the whole index
    unique(true).partialFilterExpression(Run::status.mongoEq("PENDING"))
}
```

When the fields do not all run in the same direction, `compoundIndex(vararg keys)` takes the keys
themselves. `ascKey()` / `descKey()` are the key-level counterparts of `asc()` / `desc()`: they return
one key of an index rather than a whole index.

```kotlin
compoundIndex(Event::topic.ascKey(), Event::userId.ascKey(), Event::createdAt.descKey())
compoundIndex(Event::topic.ascKey(), "payload.kind".descKey()) { unique(true) }
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
