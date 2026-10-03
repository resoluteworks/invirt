#!/bin/sh
#
# Publishes every invirt module whose current version is not yet in GitHub Packages.
#
# The publish-github-packages workflow runs this on every push to main. A version is published once:
# GitHub Packages answers 409 to a second upload of a release version, so each module is checked first
# and only the missing ones are published. That makes a re-run after a partial failure finish the job
# instead of failing on the modules that already made it, and makes a push that does not bump
# invirtVersion a no-op.
#
# Needs GITHUB_ACTOR and GITHUB_TOKEN, which GitHub Actions provides.

set -eu

repo_root=$(cd "$(dirname "$0")/.." && pwd)
registry="https://maven.pkg.github.com/resoluteworks/invirt"
group_path="dev/invirt"

: "${GITHUB_ACTOR:?publish: GITHUB_ACTOR is not set}"
: "${GITHUB_TOKEN:?publish: GITHUB_TOKEN is not set}"

version=$(sed -n 's/^invirtVersion[ \t]*=[ \t]*//p' "$repo_root/gradle.properties" | tr -d ' \r')
if [ -z "$version" ]; then
    echo "publish: no invirtVersion in $repo_root/gradle.properties" >&2
    exit 1
fi

# The modules settings.gradle.kts includes, each of which applies publish-conventions.
modules=$(sed -n 's/^include("\([^"]*\)")[ \t]*$/\1/p' "$repo_root/settings.gradle.kts" | sort)
if [ -z "$modules" ]; then
    echo "publish: no modules included in $repo_root/settings.gradle.kts" >&2
    exit 1
fi

tasks=""
for module in $modules; do
    pom="$registry/$group_path/$module/$version/$module-$version.pom"
    status=$(curl --silent --output /dev/null --write-out '%{http_code}' --user "$GITHUB_ACTOR:$GITHUB_TOKEN" "$pom")
    # GitHub Packages answers a request for a published file with a 302 to the storage holding it.
    case "$status" in
        200|302)
            echo "publish: dev.invirt:$module:$version is already published"
            ;;
        404)
            echo "publish: dev.invirt:$module:$version is missing"
            tasks="$tasks :$module:publishMavenJavaPublicationToGitHubPackagesRepository"
            ;;
        *)
            echo "publish: checking dev.invirt:$module:$version answered HTTP $status, expected 200, 302 or 404" >&2
            exit 1
            ;;
    esac
done

if [ -z "$tasks" ]; then
    echo "publish: invirt $version is already published, nothing to do"
    exit 0
fi

# One module at a time: GitHub Packages answers some uploads with 409 when several modules publish at once, and a
# module left half-published cannot be completed, because the registry refuses to overwrite a file of a release version.
# Word splitting of $tasks is what turns it into one argument per task.
# shellcheck disable=SC2086
(cd "$repo_root" && ./gradlew --no-parallel $tasks)
echo "publish: published invirt $version"
