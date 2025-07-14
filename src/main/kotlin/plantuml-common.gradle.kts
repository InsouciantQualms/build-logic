plugins {
    id("io.gitlab.plunts.plantuml")
}

val plantuml = configurations.maybeCreate("plantuml")

dependencies {
    plantuml("net.sourceforge.plantuml:plantuml:1.2023.10")
}

classDiagrams {
    plantumlServer = null
    renderClasspath(plantuml)
}
