---
sidebar_position: 9
---

# Atlas Search

Helpers for managing [Atlas Search](https://www.mongodb.com/products/platform/atlas-search) indexes
and building `Bson` aggregate stages from the driver's `SearchOperator` API. Index management uses
[Awaitility](https://github.com/awaitility/awaitility) to poll the index status.

## Constants
```kotlin
const val DEFAULT_MONGO_SEARCH_INDEX = "default"
```

## Index lifecycle
```kotlin
collection.createDefaultSearchIndex(definitionJson)
collection.recreateDefaultSearchIndex(definitionJson)
collection.recreateSearchIndex("my-index", definitionJson)

collection.waitForDefaultSearchIndexReady(seconds = 60)
collection.waitForSearchIndexReady("my-index", seconds = 60)

collection.searchIndexReady("my-index"): Boolean
```

## Waiting for documents
Useful in integration tests to give Atlas Search time to index newly-written documents.

```kotlin
collection.waitForDocumentWithIdInDefaultSearchIndex(id)
collection.waitForDocumentInDefaultSearchIndex(fieldName = "title", fieldValue = "Invirt")

collection.waitForDocumentsInDefaultSearchIndex(searchOperator, documentCount = 10)
collection.waitForDocumentsInSearchIndex(indexName, searchOperator, documentCount = 10)
```

The `_id` variant assumes the index is configured with `"_id": { "type": "string", "analyzer": "lucene.keyword" }`.

## Building aggregate stages
```kotlin
val byText = Product::title.textSearch("invirt")        // SearchOperator.text on "title"
val byPrefix = Product::name.autocomplete("invi")       // SearchOperator.autocomplete on "name"

val stage: Bson = byText.toAggregate()                  // default index name
val stage2: Bson = listOf(byText, byPrefix).toAggregate("my-index")  // compound MUST

Product::title.fieldPath()    // SearchPath.fieldPath("title")
```
