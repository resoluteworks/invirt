---
sidebar_position: 4
---

# MongoDB tests (invirt-mongodb-test)

Backed by [Testcontainers](https://www.testcontainers.org/) and Kotest's `TestContainerProjectExtension`,
so containers are started once per Kotest project and shared across specs.

### testMongo
Starts a standard MongoDB container with a replica set (required for transactions) and returns a
[`Mongo`](/docs/api/invirt-mongodb/mongo) instance pointing at a per-spec database (`uuid7()` suffix).
The instance is closed in `afterSpec`.

```kotlin
class ProductRepositoryTest : StringSpec({
    val mongo = testMongo()
    val products = mongo.database.getCollection<Product>("products")

    "inserts" {
        products.insert(Product(name = "Pear", priceMinor = 199_00))
        // ...
    }
})
```

### testMongoAtlas
Same as `testMongo`, but starts a `mongodb/mongodb-atlas-local` container so Atlas Search and other
Atlas-only features are available.

### randomTestCollection
Returns a randomly-named collection for the given document type. Useful when each test needs an
isolated collection without manual cleanup.

```kotlin
val collection: MongoCollection<Product> = mongo.randomTestCollection()
```

### Collection / document assertions
```kotlin
collection shouldHaveAscIndex "name"
collection shouldHaveDescIndex "createdAt"
collection shouldNotHaveAscIndex "internalField"
collection shouldHaveUniqueIndex "email"
collection.shouldHaveTextIndex("title", "description")
collection.shouldHaveTimestampedIndices()  // version asc, createdAt/updatedAt desc
collection.shouldHaveTtlIndex("expiresAt", expireAfterSeconds = 3600)
collection.shouldHavePartialUniqueIndex(listOf("organisationId", "email"), Document("status", "PENDING"))
collection.shouldHaveCompoundIndex("organisationId".ascKey(), "createdAt".descKey())  // exact key, 2+ fields

updated shouldBeUpdateOf previous            // updatedAt later AND version greater
updated shouldBeNextUpdateOf previous        // updatedAt later AND version == previous.version + 1
fetched shouldBeSameDocument original        // equality ignoring version + timestamps

recordsPage.idsShouldBe(listOf("a", "b"))           // any order
recordsPage.idsShouldBeInOrder("a", "b")            // exact order

collection.waitForSearchDocuments(field = "title", count = 5)  // Atlas Search readiness

document.createdAt.shouldBeAboutNow()               // within 30s of mongoNow() by default; fails on null
document.createdAt.shouldBeAboutNow(5.seconds)       // a tighter tolerance
```

### Backdating a document
`setCreatedAt` writes a document's `createdAt` directly, for a spec that needs a fixture to look older
than insertion made it - e.g. to exercise a "created in the last N days" filter. It writes raw: unlike
`update`, it does not bump `version` or touch `updatedAt`.

```kotlin
collection.setCreatedAt(id, Instant.now().minus(30, ChronoUnit.DAYS))
```

### Clearing collections between tests
`clearCollections` deletes every document from every collection except rows matching `keep` and any
collection whose name contains one of `skipCollectionNamesContaining` (the mongock bookkeeping
collections by default). It deletes documents rather than dropping collections, so indices a migration
created survive the truncation. A null `keep` deletes everything.

```kotlin
afterEach { mongo.clearCollections(keep = "createdBy".mongoEq("data-bootstrap")) }
```

### Spying on collections
`spyCollection` produces a `Mongo` whose `database.getCollection<Doc>(name)` returns a MockK spy, so
specific calls can be stubbed without affecting the rest of the application.

```kotlin
val mongoWithSpy = mongo.spyCollection<Product>("products") { collection ->
    every { collection.insertMany(any()) } throws RuntimeException("boom")
}
```
