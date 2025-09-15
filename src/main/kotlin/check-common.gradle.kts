plugins {
    java
    jacoco
}

// Helper function to create tagged test tasks
fun createTestType(
    name: String,
    tag: String,
    runAfter: TaskProvider<Test>? = null,
): TaskProvider<Test> = tasks.register<Test>(name) {
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags(tag)
    }
    reports {
        html.outputLocation = layout.buildDirectory.dir("reports/tests/$name")
    }
    runAfter?.let { shouldRunAfter(it) }
    finalizedBy(tasks.jacocoTestReport)
}

// Configure main test task
tasks.test {
    useJUnitPlatform {
        excludeTags("ArchUnitTest", "IntegrationTest", "ContainerTest", "HumanInteractiveTest")
    }
    finalizedBy(tasks.jacocoTestReport)
}

// ArchUnit tests
val archUnitTest = createTestType("archUnitTest", "ArchUnitTest", tasks.test)

// Integration tests
val integrationTest = createTestType("integrationTest", "IntegrationTest", archUnitTest)

// Container tests
val containerTest = createTestType("containerTest", "ContainerTest", integrationTest)

// Human interactive tests
val interactiveTest = createTestType("humanInteractiveTest", "HumanInteractiveTest", containerTest)

// Configure Jacoco reporting
tasks.jacocoTestReport {
    dependsOn(tasks.test, archUnitTest, integrationTest, containerTest)
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
}

// Minimum coverage
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

// Ensure that all tests run in the check phase
tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    dependsOn(archUnitTest, integrationTest, containerTest)
}
