export OP_ACCOUNT := my.1password.com
include .env
export
include gradle.properties

env:
	rm -f .env
	op read "op://Development/resolute-works-open-source/invirt.env.local" > .env

test:
	./gradlew clean test jacocoRootReport
	./gradlew coverallsJacoco

docs-serve:
	cd docs; npx docusaurus start

publish:
	./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

publish-local:
	./gradlew publish -x initializeSonatypeStagingRepository -x publishMavenJavaPublicationToSonatypeRepository

release: test publish-local publish
	@echo $(invirtVersion)
	git tag "v$(invirtVersion)" -m "Release v$(invirtVersion)"
	git push --tags --force
	@echo Finished building version $(invirtVersion)
