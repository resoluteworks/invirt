---
sidebar_position: 8
---

# Batch operations

Two `AutoCloseable` helpers for bulk-loading documents in fixed-size batches. Both flush a partial
batch on `close()`.

## MongoBatch
Batches `insertMany` operations.

```kotlin
collection.withBatch(size = 1000) { batch ->
    repeat(100_000) { i ->
        batch.add(Product(name = "p$i", priceMinor = i.toLong()))
    }
}

// or manually
MongoBatch(collection, size = 1000).use { batch ->
    batch.addAll(products)
}
```

## MongoBulkWriteBatch
Batches arbitrary `WriteModel` operations through `bulkWrite`.

```kotlin
collection.withBulkWriteBatch(size = 1000) { batch ->
    products.forEach { product ->
        batch.add(ReplaceOneModel(mongoById(product.id), product, ReplaceOptions().upsert(true)))
    }
}
```
