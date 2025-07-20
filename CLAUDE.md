
# Goal

* To create a mechanism in Gradle similar to maven archetypes for rapidly creating a template project
* Request for new Gradle projects and modules/sub-modules will be made on the command line
* Projects will contain either Java or Kotlin modules/sub-modules

# Current Instructions

* Please take a look at ~/Dev/build-logic/sample as an inspiration for the changes below:
* I do not like the approach of so much logic and magic happening in the gradle plugin or kotlin ArchetypeGenerator.kt
* Rather, the actual gradle integration itself should be minimal (and only one archetype.gradle.kts)
* I want to be able to easily edit the templates and "see" what each archetype will produce
* The same directory only has one structure (for modules), so this needs to be modified to handle modules and projects

# Constraints

* Treat the current working directory as the root of the new project or sub-module
* Precompiled scripts and version catalogs exist in build-logic
* Projects will contain a settings.gradle.kts that include build-logic as a build
* Each module/sub-module will have a build.gradle.kts that includes the appropriate precompiled script plugin
* Gradle builds will use kotlin-dsl syntax
* Any project or module created should have a minimal unit test that proves the build works successfullly
