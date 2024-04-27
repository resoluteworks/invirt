# Invirt

![GitHub release (latest by date)](https://img.shields.io/github/v/release/resoluteworks/invirt)
![Coveralls](https://img.shields.io/coverallsCoverage/github/resoluteworks/invirt)

Invirt is a framework for building web applications with [Kotlin](https://kotlinlang.org/), [http4k](https://www.http4k.org/) and server-side
rendering with [Pebble templates](https://pebbletemplates.io/).

It is **not** a framework for building microservices. 

## Motivation 
Single-page applications (SPA) have been for years one of the few _reasonable_ alternatives
to platforms like PHP or Ruby on Rails, when it comes to building web applications that aren't
software for financial services or social media mammoths.

At the same time, server-side rendering on the JVM has had a bad reputation for decades and even the most
popular frameworks in this space have done little to improve developer experience
and deliver maintainable and easy to deploy web applications. Which, in turn, has made everyone
wince at the thought of running anything "non-enterprise" on the JVM.

We believe, however, that the developments in the past years have created the space for
this thinking to be challenged.

We now have decent templating engines like [Pebble](https://pebbletemplates.io/) on the JVM. Frameworks like [http4k](https://www.http4k.org/) push the
boundaries of a modern programming language like Kotlin, and give us a great balance
between productivity and startup time. Last, but not least, with [Hotwire](https://hotwired.dev/)
we can now make modern and responsive web apps using almost exclusively
server-side rendering and little to no JavaScript.

To be clear, we believe in the diversity of the web, and we are in no way downplaying the incredible
contributions that all of these ideas, languages and platforms have made to its evolution.
This framework exists because we believe the web should evolve, it shouldn't converge, and it should
provide options and alternatives for an audience as wide as possible.
