plugins {
    java
    jacoco
}

tasks.test {
    exclude("**/*IntegrationTest*")
    exclude("**/*ContainerTest*")
    finalizedBy(tasks.jacocoTestReport)
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    include("**/*IntegrationTest*")
    shouldRunAfter(tasks.test)
    finalizedBy(tasks.jacocoTestReport)
}

val containerTest = tasks.register<Test>("containerTest") {
    description = "Runs container tests"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    include("**/*ContainerTest*")
    shouldRunAfter(integrationTest)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, integrationTest, containerTest)
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.10".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(integrationTest, containerTest)
}
