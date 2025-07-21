plugins {
    `kotlin-dsl`
}

dependencies {

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plantuml.gradle.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.shadow.plugin)
    implementation(libs.gradle.versions.plugin)
}
