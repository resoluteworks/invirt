# Invirt

![GitHub release (latest by date)](https://img.shields.io/github/v/release/resoluteworks/invirt)
![Coveralls](https://img.shields.io/coverallsCoverage/github/resoluteworks/invirt)

Invirt is a framework for building web applications with [Kotlin](https://kotlinlang.org/), [http4k](https://www.http4k.org/)
and [Pebble templates](https://pebbletemplates.io/).

Invirt doesn't re-invent the wheel. We have no intention of coming up with
our own definition of web request or HTTP handler. We simply add utility and convenience
on top of proven solutions, in order to deliver a better developer experience
for those who want to write web applications in Kotlin.

Invirt is **not** a framework for building microservices.

## Motivation
When it comes to building web applications that aren't trading systems or social media mammoths,
single-page apps (SPA) have been for years one of the few - seemingly - reasonable alternatives
to PHP or Ruby on Rails.

At the same time, server-side rendering on the JVM has had a bad reputation for decades and even the most
popular frameworks in this space have done little to improve developer experience and productivity.
Which, in turn, has made everyone wince at the thought of running anything "non-enterprise" on the JVM.

We believe, however, that the developments in the past years have created the space for
this thinking to be challenged.

We now have decent templating engines like [Pebble](https://pebbletemplates.io/) on the JVM. Frameworks like [http4k](https://www.http4k.org/) push the
boundaries of a modern programming language like Kotlin, and give us a great balance
between productivity and startup time. Last, but not least, with [Hotwire](https://hotwired.dev/)
we can make modern and responsive web apps using almost exclusively server-side rendering, and little
to no JavaScript.

To be clear, we believe in the diversity of the web, and we are in no way downplaying the incredible
contributions that all of these platforms have made to its evolution.
This framework exists because we believe the web shouldn't converge, and it should provide options and
alternatives to all audiences.
