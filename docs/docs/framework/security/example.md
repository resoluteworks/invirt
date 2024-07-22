---
sidebar_position: 2
---

import ReactPlayer from 'react-player'
import happyFlow from './assets/authentication-happy-flow.png';

# Example application
The example application is a very basic setup with a login and a dashboard screen. The so-called dashboard
is configured to only be accessible by a logged in user, and it simply displays the user's email and role.

<ReactPlayer playing controls url='/img/security-authentication-demo-app.mp4' />

<br/>
<br/>

As discussed in the [Overview](/docs/framework/security/overview) section, the setup we're after is roughly
based on the diagram below. The core Invirt Security components used in this example application
are discussed in the [previous section](/docs/framework/security/core-concepts).

<img src={happyFlow}/>

The steps 1/2/3 performed by the Authenticator implementation above would typically be delegated
to an external component or a third-party authentication provider. In order to keep this example easy
to run and reason about, we've implemented a mock in-memory user and session management in the `AuthenticationService`
component.

We recommend having a look at the [complete example](https://github.com/resoluteworks/invirt/tree/main/examples/security-authentication)
and [running it](/docs/overview/examples) to understand how all of this is wired.
