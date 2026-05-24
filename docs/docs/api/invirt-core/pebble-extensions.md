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

### EvaluationContext.request
Inside a `pebbleFunction` block, `context.request` returns the http4k `Request` for the current render.
A pre-built `requestFunction` and `errorsFunction` use this internally to expose `request()` and
`errors()` in macros.
