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

A browser application usually wants something friendlier than a bare 403 for an anonymous visitor,
though - [`redirectAnonymousToSignIn`](#redirectanonymoustosignin) below is a filter to place in
front of `securedRoutes` or `authenticatedRoutes` that redirects an anonymous navigational GET to a
sign-in page instead.

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

### redirectAnonymousToSignIn
A filter for the same anonymous-visitor case `securedRoutes`/`authenticatedRoutes` answer with
`Status.FORBIDDEN`, but redirecting to a sign-in page instead. Placed in front of one of those two,
it turns an anonymous navigational GET into a redirect built by `signInUrl` from the incoming
request - the sign-in page's location and query parameters (a return destination, a sign-up intent,
anything else) are entirely up to the application. A Turbo-Frame request (carrying a `Turbo-Frame`
header) gets a [turbo-stream redirect](/docs/api/invirt-core/hotwire#turbostreamredirect) instead of
a plain one, so the top-level page navigates rather than the frame loading the sign-in page inline.
Non-GET requests and requests that already carry a principal pass through to the filter chain that
follows.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val handler = redirectAnonymousToSignIn { request -> "/sign-in?destination=${request.pathWithQuery()}" }
        .then(
            securedRoutes<AppPrincipal>(
                check = { it.roles.contains("ADMIN") },
                route = routes(
                    "/admin" GET { Response(Status.OK) }
                )
            )
        )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun redirectAnonymousToSignIn(signInUrl: (Request) -> String): Filter
    ```
  </TabItem>
</Tabs>
