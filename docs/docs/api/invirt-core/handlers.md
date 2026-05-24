---
sidebar_position: 13
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Handlers

Ready-to-use routing handlers for common needs.

### HealthCheck.json
A `/health` route returning a JSON `{"status":"healthy"}` body. Intended for load balancer and
container orchestrator health probes.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val appHandler = routes(
        HealthCheck.json(),
        // ...
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    object HealthCheck {
        fun json(): RoutingHttpHandler
    }
    ```
  </TabItem>
</Tabs>

### staticAssets
A routing handler that serves static assets either from the filesystem (when `developmentMode = true`)
or from the classpath. See [Static assets](/docs/framework/static-assets) for the full setup.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun staticAssets(
        developmentMode: Boolean,
        classpathLocation: String = "webapp/static",
        directory: String = "src/main/resources/webapp/static"
    ): RoutingHttpHandler
    ```
  </TabItem>
</Tabs>
