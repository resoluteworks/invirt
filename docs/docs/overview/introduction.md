---
sidebar_position: 1
---

# Introduction
Invirt's objective is to provide the core MVC components and utilities to develop modern web
applications on the JVM. Invirt leverages [http4k](https://www.http4k.org/), [Pebble templates](https://pebbletemplates.io/),
and the expressiveness of Kotlin to deliver [form binding](/docs/framework/forms/form-basics), [validation](/docs/framework/forms/form-validation),
[data querying abstractions](/docs/framework/data-querying/overview), [authentication support](/docs/framework/security/overview),
and many others.

Invirt doesn't re-invent the wheel. It simply adds utility and convenience
on top of proven and mature solutions, in order to deliver a better developer experience
for those who want to write web applications in Kotlin.

Invirt is **not** a framework for building microservices.

For the in-depth documentation please check the [framework documentation](/docs/framework/configuration),
[API docs](/docs/api/invirt-core/route-binding) and the [Quick start section](/docs/overview/quickstart).

## Motivation
When it comes to building web applications that aren't trading systems or social media mammoths,
single-page apps (SPA) have been for years one of the few (seemingly) reasonable alternatives
to PHP or Ruby on Rails.

At the same time, server-side rendering on the JVM has had a bad reputation for decades and even the most
popular frameworks in this space have done little to improve developer experience and productivity.
Which, in turn, has made everyone wince at the thought of running anything "non-enterprise" on the JVM.

We believe, however, that the developments in the past years have created the space for
this thinking to be challenged.

We now have decent templating engines like [Pebble](https://pebbletemplates.io/) on the JVM. Frameworks like [http4k](https://www.http4k.org/) push the
boundaries of a modern programming language like Kotlin, and give us a great balance
between productivity and startup time. And with [Hotwire](https://hotwired.dev/)
we can build modern and responsive web apps using almost exclusively server-side rendering, and little
to no JavaScript.

To be clear, we believe in the diversity of the web, and we are in no way downplaying the incredible
contributions that all of these platforms have made to its evolution.
This framework exists because we believe the web shouldn't converge, and it should provide options and
alternatives to all audiences.

In our case, that audience are the Kotlin developers who want to build web applications.

## Why Http4k
While http4k hasn't been designed exclusively for web applications, it provides
the essential scaffolding for developing them. Http4k makes the most of Kotlin's
capabilities, and allows us to move away from the annotation-heavy MVC frameworks available
today on the JVM.

Http4k provides native support for wiring various [templating engines](https://www.http4k.org/guide/reference/templating/)
which Invirt leverages throughout the framework. We add a few conveniences here and there,
but the existing http4k templating support is more than sufficient for most applications, and doesn't
need reinventing.

Last, but not least, http4k has a very good startup time compared to other JVM frameworks
in this space.

## Why Pebble Templates
Firstly, it's important to note that one of Invirt's objectives is to provide, out of the box,
scaffolding and support to make developing web applications with Kotlin as easy as possible.

This means pushing the underlying frameworks and components towards this goal and integrating
Invirt natively with all of them. As well as minimising the need for customisation, particularly when dealing
with trivial and common use cases (like form binding, pagination, etc).

We believe that having a _bring-your-own-templating-engine_ approach would've moved us away from these
goals, and would've made it harder to support the ecosystem, for a marginal benefit. With that in mind,
we wanted to choose a JVM templating engine and go "all in", by building native Invirt integrations and utilities for it,
and expose those to the application.

The criteria we applied for choosing a JVM templating framework was:
 * It had to be actively maintained.
 * It had to have reasonable performance compared to other JVM templating frameworks.
 * It had to provide support for hot-reload: editing a template would make the changes immediately visible via a browser refresh.
  This is an important contributor to the developer experience when writing web applications, and one that comes (almost)
  for free in JavaScript/SPA ecosystems, but not so much on the JVM.
 * It had to be non-HTML-intrusive: we feel that custom HTML tags/elements are unnecessary these days, and they make reasoning about
the HTML structure harder.
 * It had to be easily extensible and allow the client code (or Invirt, in this case) to add custom functionality
that integrates easily into the HTML templating logic.

Pebble ticked all these boxes for us. While it's not unlikely for us to consider expanding Invirt to support
other templating engines, what's certain is that we won't be doing that until we see arguments against Pebble that
go beyond personal preference.
