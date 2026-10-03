# invirt

Open-source Kotlin web toolkit (http4k + Pebble + MongoDB) in the `dev.invirt` namespace. Consumed by
downstream libraries and applications.

This repository is public. Code, comments, KDoc, docs, examples, test fixtures, commit messages and branch
names never name a downstream consumer, its domain, its people or its infrastructure: call it "a downstream
library", "an application" or "a consumer", and write examples in a neutral domain (orders, articles, users).

## Publishing (read this before changing anything a consumer depends on)

invirt publishes to three places, each for a different reader:

| Where | How | Who reads it |
|---|---|---|
| **mavenLocal** | `make publish-local` (`./gradlew publishToMavenLocal`) | downstream builds on the maintainer's machine, which list mavenLocal first |
| **GitHub Packages** (`https://maven.pkg.github.com/resoluteworks/invirt`) | automatic: `.github/workflows/publish-github-packages.yml` on every push to `main` | downstream GitHub Actions runs, which have no mavenLocal |
| **Maven Central** | `make release`, by hand, and only on the maintainer's explicit say-so | everyone else |

GitHub Packages carries **every** version and Central only a deliberate subset. Central caps an organisation
at 7 releases, 1,167 files and 78 MB a month
(https://central.sonatype.org/publish/maven-central-publishing-limits/). One invirt release is about 260
files (Central lists 25 per module: jar, sources, javadoc, POM and Gradle module, each signed and
checksummed; 10 for the BOM), so the file cap allows about four releases a month, while invirt has shipped a
dozen versions in two weeks. Day-to-day versions therefore never go to Central; the downstream repos do not
need them there.

Consumers form a chain (**invirt -> downstream library -> application**). To get a change into a consumer:

1. **Bump `invirtVersion`** in `gradle.properties`. Never republish the same version: Gradle treats a release
   version as immutable and keeps serving the cached artifact, and GitHub Packages rejects a second upload of a
   version with a 409.
2. **`make publish-local`** from the invirt root. It signs the publication with the GPG key configured in
   `~/.gradle/gradle.properties` on the maintainer's machine.
3. **Bump the matching `invirtVersion` in each consumer** (every downstream library's and application's
   `gradle.properties`).
4. **Commit and push invirt to `main`** before pushing any consumer. The publish workflow uploads the new
   version to GitHub Packages (a couple of minutes; `gh run watch` on its run), and only then can the
   consumers' CI resolve it. A push that does not bump the version publishes nothing:
   `scripts/publish-github-packages.sh` checks each module's POM and publishes only the missing ones, so a
   re-run after a partial failure completes the version.

`make release` (Central) is separate from all of this: it publishes the current version to mavenLocal and to
Central through the nmcp aggregation, then tags `v<version>`, and the tag's `release.yml` creates the GitHub
release. Tags and GitHub releases therefore mark Central releases only. Never run `make release` or
`make publish` unless the maintainer asks for a Central release by name.

Signing: `publish-conventions` makes signing required only where `signing.keyId` is set. The workflow has no
key and GitHub Packages does not ask for signatures, so CI publishes unsigned; Central rejects an unsigned
upload, which is why `make release` runs on the maintainer's machine.

Reading GitHub Packages needs a token even for a public package: the consumers declare the registry with a
classic PAT with `read:packages` (exported from `~/.zshenv` locally and held as an Actions secret in each
consuming repo). The publish workflow writes with the run's own `GITHUB_TOKEN`, so invirt needs no secret.

`make test` runs `scripts/publish-github-packages.test.sh` (no network, fake `curl` and `gradlew`) after the
Gradle suite, then uploads coverage to Coveralls with the `COVERALLS_INVIRT` token exported from `~/.zshenv`.

## Conventions

- Keep library code focused: things an application should own (its git commit, its build metadata,
  deploy-time config) do not belong in the library. invirt used to expose a `gitCommitId()` helper that
  read a build-stamped `git.properties`; it was removed because a library must not bundle `git.properties`
  (it pollutes every consumer's classpath and shadows the app's own). Apps inject such values themselves
  (e.g. a `GIT_COMMIT_ID` env var read through their own config).
