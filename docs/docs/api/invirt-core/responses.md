---
sidebar_position: 8
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Response helpers

A small set of helpers for building common HTTP responses and managing cookies.

### httpSeeOther
Returns a 303 redirect response with the given `Location`.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    "/signup" POST {
        // ...
        httpSeeOther("/signup/success")
    }
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun httpSeeOther(location: String): Response
    ```
  </TabItem>
</Tabs>

### httpNotFound
Returns an empty 404 response.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun httpNotFound(): Response
    ```
  </TabItem>
</Tabs>

### htmlRedirect
Returns a 200 response with an HTML meta-refresh redirect. Useful when an OAuth callback (or another flow
with `SameSite=Strict` cookies) requires a 200 before the redirect for cookies to be sent.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun htmlRedirect(url: String): Response
    ```
  </TabItem>
</Tabs>

### Response.withCookies / Response.invalidateCookies
Add or invalidate cookies on the response. Useful in login/logout flows.
<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    Response(Status.OK).withCookies(listOf(Cookie("session", token)))
    Response(Status.OK).invalidateCookies(listOf(Cookie("session", "")))
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Response.withCookies(cookies: Collection<Cookie>): Response
    fun Response.invalidateCookies(cookies: Collection<Cookie>): Response
    ```
  </TabItem>
</Tabs>

### Request.cookiesFrom / Request.withCookies
Copies cookies onto a `Request`, useful for handler composition and testing.
<Tabs>
  <TabItem value="declaration" label="Declaration" default>
    ```kotlin
    fun Request.cookiesFrom(response: Response): Request
    fun Request.withCookies(cookies: List<Cookie>): Request
    ```
  </TabItem>
</Tabs>
