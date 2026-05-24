---
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Authenticator

See [Security &mdash; Core concepts](/docs/framework/security/core-concepts#authenticator) for an overview.

### Authenticator
Functional interface implemented by the application to map an HTTP request to an authentication outcome.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun interface Authenticator {
        fun authenticate(request: Request): AuthenticationResponse
    }
    ```
  </TabItem>
</Tabs>

### AuthenticationResponse
Sealed result type returned by an `Authenticator`.

<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    sealed class AuthenticationResponse {
        data object Unauthenticated : AuthenticationResponse()

        data class Authenticated<P : Principal>(
            val principal: P,
            val newCookies: List<Cookie> = emptyList()
        ) : AuthenticationResponse()
    }
    ```
  </TabItem>
</Tabs>

`newCookies` are set on the response after the request completes &mdash; useful for setting login cookies
or refreshing rolling JWT tokens.
