export OP_ACCOUNT := my.1password.com
include gradle.properties

ifneq (,$(wildcard ./.env))
    include .env
    export
endif

env:
	rm -f .env
	op read "op://Development/resolute-works-open-source/invirt.env.local" > .env

test:
	./gradlew clean test jacocoRootReport
	./gradlew coverallsJacoco

docs-serve:
	cd docs; npx docusaurus start

docs-build:
	cd docs; npm install; npm run build

publish:
	./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

publish-local:
	./gradlew publish -x initializeSonatypeStagingRepository -x publishMavenJavaPublicationToSonatypeRepository

release: test publish-local publish
	@echo $(invirtVersion)
	git tag "v$(invirtVersion)" -m "Release v$(invirtVersion)"
	git push --tags --force
	@echo Finished building version $(invirtVersion)
