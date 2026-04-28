plugins {
    `kotlin-dsl`
}

dependencies {

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.plantuml.gradle.plugin)
    implementation(libs.spotless.plugin)
    implementation(libs.shadow.plugin)
    implementation(libs.gradle.versions.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.sqldelight.gradle.plugin)
}
