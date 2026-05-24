---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Principal

See [Security &mdash; Core concepts](/docs/framework/security/core-concepts#principal) for an overview.

### Principal
Marker interface for the authenticated entity, requiring a stable reference.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    interface Principal {
        val ref: PrincipalRef
    }

    data class PrincipalRef(val type: String, val id: String) {
        override fun toString(): String = "$type:$id"  // e.g. "user:abc123"
    }
    ```
  </TabItem>
</Tabs>

### Request.principal
Returns the `Principal` attached to the request via [`AuthenticationFilter`](/docs/api/invirt-security/authentication-filter)
(or `withPrincipal()`), or `null` when no principal is attached.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    val Request.principal: Principal?
    ```
  </TabItem>
</Tabs>

### Request.hasPrincipal
True when a principal is attached to the request.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    val Request.hasPrincipal: Boolean
    ```
  </TabItem>
</Tabs>

### Request.withPrincipal
Returns a copy of the request with the given principal attached. Primarily intended for testing
handlers that depend on an authenticated principal &mdash; in production code the principal is set by
`AuthenticationFilter`.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun Request.withPrincipal(principal: Principal): Request
    ```
  </TabItem>
</Tabs>
