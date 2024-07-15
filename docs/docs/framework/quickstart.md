---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Quick start
Invirt comes as a set of libraries, discussed throughout this documentation, which can be added incrementally
as you expand the areas you want to cover with your application. Most of the functionality, however, is contained
in the core library which can be added as per Gradle example below.

```kotlin
implementation(platform("io.resoluteworks:invirt-bom:${invirtVersion}"))
implementation("io.resoluteworks:invirt-core")
```

Below is the structure of a very basic project with Invirt. For a full example check...

```text
├── build.gradle.kts
└── src
    └── main
        ├── kotlin
        │   └── examples
        │       └── quickstart
        │           └── Application.kt
        └── resources
            └── webapp
                └── views
                    ├── index.peb
                    └── layout.peb
```

