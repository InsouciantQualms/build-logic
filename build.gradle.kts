plugins {
    `kotlin-dsl`
}

/*
sourceSets {
    main {
        kotlin {
            srcDir("archetype")
        }
    }
}
*/

dependencies {

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plantuml.gradle.plugin)
    implementation(libs.spotless.plugin)
}
