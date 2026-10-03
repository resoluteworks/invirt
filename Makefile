include gradle.properties

test:
	./gradlew clean test jacocoRootReport
	./scripts/publish-github-packages.test.sh
	./gradlew coverallsJacoco

docs-serve:
	cd docs && npx docusaurus start

docs-build:
	cd docs && npm install && npm run build

publish:
	./gradlew publishAggregationToCentralPortal

publish-local:
	./gradlew publishToMavenLocal

release: publish-local publish
	@echo $(invirtVersion)
	git tag "v$(invirtVersion)" -m "Release v$(invirtVersion)"
	git push --tags --force
	@echo Finished building version $(invirtVersion)
