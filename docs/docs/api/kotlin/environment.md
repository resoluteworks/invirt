---
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Environment

### Environment.developmentMode
Reads a variable `DEVELOPMENT_MODE` from the receiver [Environment](https://www.http4k.org/api/org.http4k.cloudnative.env/-environment/) which can be
set on your local machine when running the application locally to enable hot reload capabilities
(browser refresh loads template edits or static asset edits).

`Environment.developmentMode` defaults to `false` so in a production environment its absence implicitly
enables classpath loading for views or static assets.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val devMode = Environment.ENV.developmentMode
    initialiseInvirtViews(hotReload = devMode)
    val appHandler = InvirtFilter().then(
        routes(
            "/static/${assetsVersion}" bind staticAssets(devMode)
        )
    )
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    val Environment.developmentMode: Boolean get() = EnvironmentKey.boolean().defaulted("DEVELOPMENT_MODE", false)(this)
    ```
  </TabItem>
</Tabs>


### Environment.withDotEnv()
Loads environment variables from .env files and returns a new [Environment](https://www.http4k.org/api/org.http4k.cloudnative.env/-environment/)
with the combined variables from receiver environment and the `.env` file. The variables in the receiver `Environment`
override the ones in the .env files.

An optional directory path argument (defaulting to `./`)  can be passed to specify the location where to look up
the .env files.

Invirt uses the [dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin) library underneath. Please see
next section for customising Dotenv loading.

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    // Environment containing system env vars combined with the ones in ./.env
    val env = Environment.ENV.withDotEnv()

    // Environment containing system env vars combined with the ones in /home/user/.env
    val env = Environment.ENV.withDotEnv("/home/user")
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Environment.withDotEnv(dotEnvDirectory: String = "./"): Environment
    ```
  </TabItem>
</Tabs>

### Environment.withDotEnv(Dotenv)
Allows overriding the settings the dotenv-kotlin uses to load .env files. You must add the [dotenv-kotlin](https://github.com/cdimascio/dotenv-kotlin)
dependency to your project to use this

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    ```
    ```kotlin
    val dotEnv = dotenv {
        directory = "../../"
        ignoreIfMissing = false
        systemProperties = true
    }

    val env = Environment.ENV.withDotEnv(dotEnv)
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun Environment.withDotEnv(dotEnv: Dotenv): Environment
    ```
  </TabItem>
</Tabs>

### gitCommitId()
Returns the Git commit id, read from a property named `git.commit.id` in a `git.properties` in the classpath.
The call fails if `git.properties` cannot be found and returns `null` if the file doesn't contain a `git.commit.id` property.

A `git.propreties` can be created by your application's build, or more commonly by using a Gradle plugin
like [gradle-git-properties](https://github.com/n0mer/gradle-git-properties).

```kotlin
plugins {
    id "com.gorylenko.gradle-git-properties" version "2.4.2"
}
```

<Tabs>
  <TabItem value="example" label="Example" default>
    ```kotlin
    val assetsVersion = gitCommitId()
    ```
  </TabItem>
  <TabItem value="declaration" label="Declaration">
    ```kotlin
    fun gitCommitId(): String? = EnvironmentKey.optional("git.commit.id")(Environment.fromResource("git.properties"))
    ```
  </TabItem>
</Tabs>
