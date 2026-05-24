---
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# AuthenticationFilter

The http4k `Filter` that runs the application's [`Authenticator`](/docs/api/invirt-security/authenticator)
on each request. When the authenticator returns `Authenticated`, the principal is attached to the
request via [`Request.withPrincipal`](/docs/api/invirt-security/principal#requestwithprincipal) and any
`newCookies` are applied to the response. When the authenticator returns `Unauthenticated`, the request
is forwarded unchanged &mdash; access control is left to downstream routes (e.g.
[`securedRoutes`](/docs/api/invirt-security/secured-routes)).

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val appHandler = AuthenticationFilter(authenticator)
        .then(routes(/* ... */))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    object AuthenticationFilter {
        operator fun invoke(authenticator: Authenticator): Filter
    }
    ```
  </TabItem>
</Tabs>
