#!/bin/sh
#
# Tests for publish-github-packages.sh.
#
# Each case builds a throwaway repo with the script, a gradle.properties and a settings.gradle.kts,
# puts a fake curl on PATH that answers each POM URL from a table, and a fake gradlew that records the
# tasks it was asked to run. Nothing touches the network or the real build.

set -eu

script="$(cd "$(dirname "$0")" && pwd)/publish-github-packages.sh"
failures=0

# Builds a fake repo in a new temp directory and prints its path.
#   $1: invirtVersion line for gradle.properties ("" for none)
#   $2: space-separated modules to include in settings.gradle.kts
#   $3: newline-separated "<module> <http status>" answers for the fake curl
make_repo() {
    repo=$(mktemp -d)
    mkdir -p "$repo/scripts" "$repo/bin"
    cp "$script" "$repo/scripts/"
    printf '%s\nkotlinVersion = 2.0.0\n' "$1" > "$repo/gradle.properties"
    printf 'rootProject.name = "invirt"\n\n' > "$repo/settings.gradle.kts"
    for module in $2; do
        printf 'include("%s")\n' "$module" >> "$repo/settings.gradle.kts"
    done
    printf '%s\n' "$3" > "$repo/answers"

    cat > "$repo/bin/curl" <<EOS
#!/bin/sh
for arg; do url=\$arg; done
echo "\$url" >> "$repo/curl.log"
module=\$(echo "\$url" | sed -n 's|.*/dev/invirt/\([^/]*\)/.*|\1|p')
status=\$(awk -v m="\$module" '\$1 == m { print \$2 }' "$repo/answers")
printf '%s' "\${status:-404}"
EOS
    cat > "$repo/gradlew" <<EOS
#!/bin/sh
echo "\$*" > "$repo/gradle.log"
EOS
    chmod +x "$repo/bin/curl" "$repo/gradlew"
    echo "$repo"
}

run() {
    PATH="$1/bin:$PATH" GITHUB_ACTOR=actor GITHUB_TOKEN=token "$1/scripts/publish-github-packages.sh" > "$1/out" 2>&1
}

pass() { echo "ok   $1"; }
fail() { echo "FAIL $1"; [ -f "$2/out" ] && sed 's/^/     /' "$2/out"; failures=$((failures + 1)); }

# Nothing published yet: every module is published, in one Gradle run.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-core invirt-bom" "")
if run "$repo" && [ "$(cat "$repo/gradle.log")" = "--no-parallel :invirt-bom:publishMavenJavaPublicationToGitHubPackagesRepository :invirt-core:publishMavenJavaPublicationToGitHubPackagesRepository" ]; then
    pass "publishes every module when none is published"
else
    fail "publishes every module when none is published" "$repo"
fi

# The POM URL carries the version and the module coordinates.
if grep -qx "https://maven.pkg.github.com/resoluteworks/invirt/dev/invirt/invirt-core/1.2.3/invirt-core-1.2.3.pom" "$repo/curl.log"; then
    pass "checks the module's POM at the current version"
else
    fail "checks the module's POM at the current version" "$repo"
fi

# A partial earlier run: only the missing module is published.
repo=$(make_repo "invirtVersion=1.2.3" "invirt-bom invirt-core" "invirt-bom 200")
if run "$repo" && [ "$(cat "$repo/gradle.log")" = "--no-parallel :invirt-core:publishMavenJavaPublicationToGitHubPackagesRepository" ]; then
    pass "publishes only the modules that are missing"
else
    fail "publishes only the modules that are missing" "$repo"
fi

# Everything already published: Gradle does not run at all.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-bom invirt-core" "invirt-bom 200
invirt-core 200")
if run "$repo" && [ ! -f "$repo/gradle.log" ] && grep -q "nothing to do" "$repo/out"; then
    pass "does nothing when the version is already published"
else
    fail "does nothing when the version is already published" "$repo"
fi

# GitHub Packages answers for a published file with a redirect to its storage, which counts as published.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-bom invirt-core" "invirt-bom 302
invirt-core 302")
if run "$repo" && [ ! -f "$repo/gradle.log" ] && grep -q "nothing to do" "$repo/out"; then
    pass "treats a redirect to the file as published"
else
    fail "treats a redirect to the file as published" "$repo"
fi

# Only include("...") lines name modules: comments and other settings are not modules.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-core" "")
printf '// include("invirt-old")\nplugins { id("foo") }\n' >> "$repo/settings.gradle.kts"
if run "$repo" && [ "$(cat "$repo/gradle.log")" = "--no-parallel :invirt-core:publishMavenJavaPublicationToGitHubPackagesRepository" ]; then
    pass "reads modules only from include lines"
else
    fail "reads modules only from include lines" "$repo"
fi

# Any answer other than 200, 302 or 404 (a bad token, a registry outage) fails before publishing anything.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-bom invirt-core" "invirt-bom 401")
if ! run "$repo" && [ ! -f "$repo/gradle.log" ] && grep -q "HTTP 401" "$repo/out"; then
    pass "fails on an unexpected registry answer"
else
    fail "fails on an unexpected registry answer" "$repo"
fi

# No version: fails rather than checking for an empty one.
repo=$(make_repo "" "invirt-core" "")
if ! run "$repo" && [ ! -f "$repo/gradle.log" ] && grep -q "no invirtVersion" "$repo/out"; then
    pass "fails without an invirtVersion"
else
    fail "fails without an invirtVersion" "$repo"
fi

# No modules: fails rather than reporting success for nothing.
repo=$(make_repo "invirtVersion = 1.2.3" "" "")
if ! run "$repo" && grep -q "no modules" "$repo/out"; then
    pass "fails without modules"
else
    fail "fails without modules" "$repo"
fi

# No credentials: fails before any request.
repo=$(make_repo "invirtVersion = 1.2.3" "invirt-core" "")
if ! PATH="$repo/bin:$PATH" GITHUB_ACTOR=actor "$repo/scripts/publish-github-packages.sh" > "$repo/out" 2>&1 \
    && [ ! -f "$repo/curl.log" ] && grep -q "GITHUB_TOKEN is not set" "$repo/out"; then
    pass "fails without GITHUB_TOKEN"
else
    fail "fails without GITHUB_TOKEN" "$repo"
fi

if [ "$failures" -gt 0 ]; then
    echo "$failures failed"
    exit 1
fi
echo "all passed"
