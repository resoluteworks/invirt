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

generate-project:
	rm -rf test
	copier copy \
		-d 'project_name=libb' \
		-d 'kotlin_version=1.9.23' \
		-d 'jdk_version=21' \
		-d 'invirt_version=0.4.5' \
		-d 'font_sans=Inter' \
		project-template test
	cd test/libb; ./gradlew build
