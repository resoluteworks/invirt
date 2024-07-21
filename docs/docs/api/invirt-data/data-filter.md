---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# DataFilter

This is an abstract and `sealed` component for defining data querying logic separate from an application's
database semantics. The rationale for this is discussed [here](/docs/framework/data-querying/overview#rationale).

The component has two subclasses `DataFilter.Field` and `DataFilter.Compound`, which represent definitions
for direct field filtering criteria and compound (OR/AND) queries, respectively

## DataFilter.Field
Defines a data querying criteria in the form of `<fieldName> <operation> [optional <value>]`. This is typically
used to define equals, greater than, less than, contains, etc filtering criteria.

```kotlin
data class Field<Value : Any>(
    val field: String,
    val operation: Operation,
    val value: Value
) : DataFilter()
```

A set of utilities and extension functions can be used to construct field filters for various types
of matching criteria.

```kotlin
// Filter records where "size" is greater than 20
val filter = "size".gt(20)

// Filter records where "status" is not equal to Status.PUBLISHED
Document::status.ne(Status.PUBLISHED)
```

## DataFilter.Compound
Combines a set of DataFilter objects with a logical AND or OR criteria.
```kotlin
data class Compound(
    val operator: Operator,
    val subFilters: Collection<DataFilter>
) : DataFilter()
```

Below is an example of a compound filter using a set of built-in utilities. Although, in
an Invirt application, when these objects are being read from [query parameters](/docs/framework/data-querying/example#filtering-logic),
you wouldn't normally have to construct these objects manually.

```kotlin
val filter = orFilter(
    Document::status.ne(Status.PUBLISHED),
    andFilter(
        Document::status.eq(Status.PUBLISHED),
        Document::size.gte(200000)
    )
)
```
