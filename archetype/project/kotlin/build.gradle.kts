plugins {
    base
}

description = "{{projectName}} root project"

tasks.register("clean") {
    doLast {
        delete(rootProject.layout.buildDirectory)
    }
}