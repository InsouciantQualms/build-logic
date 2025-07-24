
# Goal

* To create a mechanism in Gradle similar to maven archetypes for rapidly creating a template project
* Request for new Gradle projects and modules/sub-modules will be made on the command line
* Projects will contain either Java or Kotlin modules/sub-modules

# Current Instructions



# Constraints

* Treat the current working directory as the root of the new project or sub-module
* Precompiled scripts and version catalogs exist in build-logic
* Projects will contain a settings.gradle.kts that include build-logic as a build
* Each module/sub-module will have a build.gradle.kts that includes the appropriate precompiled script plugin
* Gradle builds will use kotlin-dsl syntax
* Any project or module created should have a minimal unit test that proves the build works successfullly
