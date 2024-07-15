---
sidebar_position: 1
---

# InvirtRequest

class [InvirtRequest](index.md)(val delegate: Request) : Request

A thin wrapper of http4k's Request object.

#### Parameters


| | |
|---|---|
| delegate | the underlying http4k Request |

## Constructors

| | |
|---|---|
| [InvirtRequest](-invirt-request.md) | <br/>constructor(delegate: Request) |

## Properties

| Name | Summary |
|---|---|
| [delegate](delegate.md) | <br/>val [delegate](delegate.md): Request |

## Functions

| Name | Summary |
|---|---|
| [hasQueryValue](has-query-value.md) | <br/>fun [hasQueryValue](has-query-value.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br/>Checks whether this request's URI has a query parameter with the specified [name](has-query-value.md) and [value](has-query-value.md) |
| [removeQueries](remove-queries.md) | <br/>fun [removeQueries](remove-queries.md)(names: [Array](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-array/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;): Uri<br/>fun [removeQueries](remove-queries.md)(names: [Collection](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-collection/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;): Uri |
| [removeQueryValue](remove-query-value.md) | <br/>fun [removeQueryValue](remove-query-value.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): Uri |
| [replacePage](replace-page.md) | <br/>fun [replacePage](replace-page.md)(page: Page): Uri |
| [replaceQuery](replace-query.md) | <br/>fun [replaceQuery](replace-query.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): Uri |
| [toggleQueryValue](toggle-query-value.md) | <br/>fun [toggleQueryValue](toggle-query-value.md)(name: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), value: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): Uri |
