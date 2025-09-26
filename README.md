/*
 * Insouciant Qualms © 2025 by Sascha Goldsmith is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0.
 * To reach the creator, visit https://www.linkedin.com/in/saschagoldsmith.
 */
# Common Gradle Build Logic

This module acts as build logic (externalized buildSrc) containing:

- Centralized version catalogs, 
- Pre-compiled build scripts (plugins) for Java, Kotlin, build consistency checks and more
- Archetype generator to quickly create Gradle projects and sub-modules.

To leverage this build logic, add the following stanzas to your settings.gradle.kts:

```kotlin
@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        google()
        // gradlePluginPortal()
    }
    versionCatalogs {

        create("libs") {
            from(files("../build-logic/libs.versions.toml"))
        }
    }
}

includeBuild("../build-logic")
```

Note:  Uncomment gradlePluginPortal() if you plan on adding third-party plugins to your build
