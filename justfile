build:
  ./gradlew build

test:
  ./gradlew test

publish-local:
  ./gradlew publishToMavenLocal

[doc("Run the linter check.")]
lintcheck:
  ./gradlew ktlintCheck

[doc("Run the linter formatter.")]
lintformat:
  ./gradlew ktlintFormat
