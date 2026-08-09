# invirt

Open-source Kotlin web toolkit (http4k + Pebble + MongoDB) in the `dev.invirt` namespace. Consumed by
appkit and downstream apps (e.g. Curioso).

## Local publishing (read this before changing anything a consumer depends on)

invirt is published to **Maven Central**, but locally it is consumed from **mavenLocal**, which downstream
builds (appkit, apps) list *before* mavenCentral. So a local change flows through mavenLocal, not Central.

The chain is **invirt → appkit → app** (Curioso). Each hop resolves the one below from mavenLocal first.

To get a change into a consumer:

1. **Bump `invirtVersion`** in `gradle.properties`. Do not republish the same version - Gradle treats a
   release version as immutable and will keep serving the cached (Central or previous-local) artifact, so
   the consumer silently won't see your change. Bumping forces clean re-resolution.
2. **`make publish-local`** (from the invirt root) - this runs `./gradlew publish`, whose only configured
   repository is `mavenLocal()` (see `buildSrc/.../publish-conventions.gradle.kts`). It signs the
   publication, so a GPG signing key must be configured (it is on the maintainer's machine).
3. **Bump the matching `invirtVersion` in the consumer** (appkit's and/or the app's `gradle.properties`)
   to the new version, so it resolves your freshly-published local build.

A locally-published version exists only in mavenLocal until you cut a real release (`make release`, which
tags and lets CI publish to Central). Until then, other machines / CI can't resolve it - keep that in mind
before depending on a local-only bump from something that has to build elsewhere.

## Conventions

- Keep library code focused: things an application should own (its git commit, its build metadata,
  deploy-time config) do not belong in the library. invirt used to expose a `gitCommitId()` helper that
  read a build-stamped `git.properties`; it was removed because a library must not bundle `git.properties`
  (it pollutes every consumer's classpath and shadows the app's own). Apps inject such values themselves
  (e.g. a `GIT_COMMIT_ID` env var read through their own config).
