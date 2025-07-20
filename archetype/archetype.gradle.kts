buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
        classpath(files("../build/libs/build-logic.jar"))
    }
}

tasks.register("createProject") {
    group = "archetype"
    description = "Create a new Gradle project"
    
    doLast {
        val projectName = project.findProperty("projectName")?.toString()
            ?: throw GradleException("Please specify project name: -PprojectName=<name>")
        
        val language = project.findProperty("language")?.toString() ?: "java"
        
        System.setProperty("archetype.root", file(".").absolutePath)
        
        javaexec {
            classpath = buildscript.configurations["classpath"]
            mainClass.set("ArchetypeGeneratorKt")
            args("create-project", projectName, language)
        }
    }
}

tasks.register("createModule") {
    group = "archetype"
    description = "Create a new module"
    
    doLast {
        val moduleName = project.findProperty("moduleName")?.toString()
            ?: throw GradleException("Please specify module name: -PmoduleName=<name>")
        
        val language = project.findProperty("language")?.toString() ?: "java"
        val packageName = project.findProperty("packageName")?.toString()
        
        System.setProperty("archetype.root", file(".").absolutePath)
        
        val argsList = mutableListOf("create-module", moduleName, language)
        packageName?.let { argsList.add(it) }
        
        javaexec {
            classpath = buildscript.configurations["classpath"]
            mainClass.set("ArchetypeGeneratorKt")
            args(argsList)
        }
    }
}