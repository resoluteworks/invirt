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

updated shouldBeUpdateOf previous            // updatedAt later AND version greater
updated shouldBeNextUpdateOf previous        // updatedAt later AND version == previous.version + 1
fetched shouldBeSameDocument original        // equality ignoring version + timestamps

recordsPage.idsShouldBe(listOf("a", "b"))           // any order
recordsPage.idsShouldBeInOrder("a", "b")            // exact order

collection.waitForSearchDocuments(field = "title", count = 5)  // Atlas Search readiness
```

### Spying on collections
`spyCollection` produces a `Mongo` whose `database.getCollection<Doc>(name)` returns a MockK spy, so
specific calls can be stubbed without affecting the rest of the application.

```kotlin
val mongoWithSpy = mongo.spyCollection<Product>("products") { collection ->
    every { collection.insertMany(any()) } throws RuntimeException("boom")
}
```
