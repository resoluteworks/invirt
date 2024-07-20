---
sidebar_position: 1
---

# Overview

Invirt Data is a small library defining a set of components like filters, pagination,
and sorting, which serve as abstractions for querying an underlying database.

Invirt applications can use this abstraction to derive querying logic from HTTP requests
(using URL query parameters, for example) via native Invirt capabilities. These abstract
models can then be used by an application to construct database-specific queries.

## Dependency
```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-data")
```
