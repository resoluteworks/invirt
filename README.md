# Invirt

![GitHub release (latest by date)](https://img.shields.io/github/v/release/resoluteworks/invirt)
![Coveralls](https://img.shields.io/coverallsCoverage/github/resoluteworks/invirt)

Invirt is a framework for building web applications with [Kotlin](https://kotlinlang.org/), [http4k](https://www.http4k.org/) and server-side
rendering with [Pebble templates](https://pebbletemplates.io/).

It is **not** a framework for building microservices. 

## Motivation 
JavaScript-heavy single-page applications (SPA) have been for years the only _reasonable_ alternatives
to PHP and Ruby on Rails, when it comes to building web applications.

At the same time, server-side rendering on the JVM has had a bad reputation for decades and even the most
popular frameworks in this space have done little to improve developer experience
and deliver maintainable and easy to deploy web applications. Which, in turn, has made everyone
wince at the thought of running anything "non-enterprise" on the JVM.

We believe, however, that the developments in the past years have created an environment where
this thinking can be challenged.

We now have decent templating engines like [Pebble](https://pebbletemplates.io/)
(and many others) on the JVM. Frameworks like [http4k](https://www.http4k.org/) push the
boundaries of a modern programming language (Kotlin) and create a great balance
between developer experience and startup time. Last, but not least, [Hotwire](https://hotwired.dev/)
enables us to produce modern and responsive web applications using almost exclusively
server-side rendering and little to no JavaScript.
