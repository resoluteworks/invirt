---
sidebar_position: 14
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Pebble extensions

Helpers for building custom [Pebble extensions](https://pebbletemplates.io/wiki/guide/extending-pebble/).
Register them through [`InvirtPebbleConfig.extensions`](/docs/framework/configuration#pebble-configuration).

### pebbleFunction
Builds a `PebbleFunction` from a Kotlin lambda. The lambda receives a `PebbleFunctionExecutionContext`
with the call arguments, current template, evaluation context and line number.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val currentUserFn = pebbleFunction("currentUser") {
        request.principal as? User
    }

    val greetFn = pebbleFunction("greet", "name") {
        "Hello, ${args["name"]}"
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun pebbleFunction(
        name: String,
        vararg argumentNames: String,
        block: PebbleFunctionExecutionContext.() -> Any?
    ): PebbleFunction

    class PebbleFunctionExecutionContext(
        val args: Map<String, Any>,
        val template: PebbleTemplate,
        val context: EvaluationContext,
        val lineNumber: Int
    )
    ```
  </TabItem>
</Tabs>

### pebbleFilter
Builds a `PebbleFilter` from a Kotlin lambda, the filter-side equivalent of `pebbleFunction`. The filtered
value is the lambda's argument and the named arguments are on the receiver, along with the filter name and
the template line a failure points at.

A null input reaches the lambda rather than short-circuiting to null: Pebble applies a filter to a null
value too, and whether that is nothing to do or an error is the filter's own decision.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val markdownFilter = pebbleFilter("markdown") { input ->
        input?.let { SafeString(MarkdownRenderer.toHtml(it as String)) }
    }

    val truncateFilter = pebbleFilter("truncate", "length") { input ->
        val length = (args["length"] as? Number)?.toInt()
            ?: throw PebbleException(null, "Filter [$name] needs a length", lineNumber, template.name)
        input?.toString()?.take(length)
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun pebbleFilter(
        name: String,
        vararg argumentNames: String,
        apply: PebbleFilterExecutionContext.(input: Any?) -> Any?
    ): PebbleFilter

    class PebbleFilterExecutionContext(
        val name: String,
        val args: Map<String, Any?>,
        val template: PebbleTemplate,
        val context: EvaluationContext,
        val lineNumber: Int
    )
    ```
  </TabItem>
</Tabs>

### pebbleFunctions
Bundles one or more `PebbleFunction` instances into an `Extension` that can be passed to
`InvirtPebbleConfig`.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    Invirt.configure(
        pebble = InvirtPebbleConfig(
            extensions = listOf(
                pebbleFunctions(currentUserFn, greetFn)
            )
        )
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun pebbleFunctions(vararg functions: PebbleFunction): Extension
    ```
  </TabItem>
</Tabs>

### pebbleFilters
The same for filters: bundles one or more `PebbleFilter` instances into an `Extension`.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    Invirt.configure(
        pebble = InvirtPebbleConfig(
            extensions = listOf(
                pebbleFilters(markdownFilter, truncateFilter)
            )
        )
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun pebbleFilters(vararg filters: PebbleFilter): Extension
    ```
  </TabItem>
</Tabs>

### EvaluationContext.request
Inside a `pebbleFunction` block, `context.request` returns the http4k `Request` for the current render.
A pre-built `requestFunction` and `errorsFunction` use this internally to expose `request()` and
`errors()` in macros.
