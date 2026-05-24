---
sidebar_position: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Secured routes

Two helpers for restricting access to a set of routes once an [`AuthenticationFilter`](/docs/api/invirt-security/authentication-filter)
is in place. Both return `Response(Status.FORBIDDEN)` when the access check fails. To return a 404
instead (to avoid revealing the existence of protected URLs), combine with
[`StatusOverride`](/docs/framework/filters#statusoverride) and
[`ErrorPages`](/docs/framework/filters#errorpages).

### authenticatedRoutes
Requires that a `Principal` is attached to the request.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val handler = authenticatedRoutes(
        DashboardHandler(),
        LogoutHandler()
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun authenticatedRoutes(vararg routes: RoutingHttpHandler): RoutingHttpHandler
    ```
  </TabItem>
</Tabs>

### securedRoutes
Requires that the request's principal is an instance of `P` *and* passes the `check` predicate.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val handler = securedRoutes<AppPrincipal>(
        check = { it.roles.contains("ADMIN") },
        route = routes(
            "/admin" GET { Response(Status.OK) }
        )
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    inline fun <reified P : Principal> securedRoutes(
        crossinline check: (P) -> Boolean,
        route: RoutingHttpHandler
    ): RoutingHttpHandler
    ```
  </TabItem>
</Tabs>
