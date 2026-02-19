[doc("Default command; list all available commands.")]
@list:
  just --list --unsorted

[doc("Build the library.")]
build:
  ./gradlew build

[doc("Delete all previously built artifacts.")]
clean:
  rm -rf build/

[doc("Build Dokka docs.")]
docs:
  ./gradlew dokkaGeneratePublicationHtml
  cd build/dokka/html/ && python3 -m http.server 8000

[doc("Run all tests, unless a specific test is provided.")]
test *TEST:
  ./gradlew test {{ if TEST == "" { "" } else { "--tests " + TEST } }}

[doc("Publish the library to local Maven repository.")]
publish-local:
  ./gradlew publishToMavenLocal

[doc("Run the linter check.")]
lintcheck:
  ./gradlew ktlintCheck

[doc("Run the linter formatter.")]
lintformat:
  ./gradlew ktlintFormat
