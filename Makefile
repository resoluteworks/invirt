export OP_ACCOUNT := my.1password.com
include .env
export

env:
	rm -f .env
	op read "op://Development/resolute-works-open-source/invirt.env.local" > .env

test:
	./gradlew clean test
	./gradlew jacocoRootReport

publish:
	./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

publish-local:
	./gradlew publish -x initializeSonatypeStagingRepository -x publishMavenJavaPublicationToSonatypeRepository
