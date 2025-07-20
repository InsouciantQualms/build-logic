import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

object ArchetypeGenerator {
    
    fun generate(
        templateType: String,  // "project" or "module"
        language: String,      // "java" or "kotlin"
        targetName: String,
        packageName: String? = null
    ) {
        val templateRoot = findTemplateRoot()
        val templateDir = templateRoot.resolve("$templateType/$language")
        
        if (!templateDir.exists()) {
            error("Template not found: $templateType/$language")
        }
        
        val targetPath = Paths.get(".").resolve(targetName)
        if (targetPath.exists()) {
            error("Target already exists: $targetPath")
        }
        
        val actualPackageName = packageName ?: derivePackageName(targetName)
        val packagePath = actualPackageName.replace('.', '/')
        val className = toClassName(targetName)
        
        val replacements = mapOf(
            "{{projectName}}" to targetName,
            "{{packageName}}" to actualPackageName,
            "{{packagePath}}" to packagePath,
            "{{className}}" to className
        )
        
        copyTemplate(templateDir, targetPath, replacements)
        
        // Update parent settings if creating a module
        if (templateType == "module") {
            updateParentSettings(targetName)
        }
    }
    
    private fun findTemplateRoot(): Path {
        val locations = listOf(
            Paths.get("archetype"),
            Paths.get("build-logic/archetype"),
            Paths.get("../build-logic/archetype"),
            Paths.get(System.getProperty("archetype.root", "archetype"))
        )
        
        return locations.firstOrNull { it.exists() }
            ?: error("Cannot find archetype templates directory")
    }
    
    private fun copyTemplate(source: Path, target: Path, replacements: Map<String, String>) {
        Files.walk(source).forEach { sourcePath ->
            val relativePath = source.relativize(sourcePath).toString()
            var targetPathStr = target.resolve(relativePath).toString()
            
            // Replace placeholders in path names
            replacements.forEach { (placeholder, value) ->
                targetPathStr = targetPathStr.replace(placeholder, value)
            }
            
            val targetPath = Paths.get(targetPathStr)
            
            when {
                sourcePath.isDirectory() -> {
                    targetPath.createDirectories()
                }
                sourcePath.isRegularFile() -> {
                    targetPath.parent?.createDirectories()
                    
                    if (sourcePath.fileName.toString().endsWith(".jar") || 
                        sourcePath.fileName.toString().endsWith(".png") ||
                        sourcePath.fileName.toString().endsWith(".jpg")) {
                        // Binary files - copy as-is
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    } else {
                        // Text files - process replacements
                        var content = sourcePath.readText()
                        replacements.forEach { (placeholder, value) ->
                            content = content.replace(placeholder, value)
                        }
                        targetPath.writeText(content)
                    }
                }
            }
        }
    }
    
    private fun updateParentSettings(moduleName: String) {
        val settingsFile = Paths.get("settings.gradle.kts")
        if (settingsFile.exists()) {
            val content = settingsFile.readText()
            if (!content.contains("include(\"$moduleName\")")) {
                settingsFile.writeText(content + "\ninclude(\"$moduleName\")")
            }
        }
    }
    
    private fun derivePackageName(name: String): String {
        val normalized = name.lowercase()
            .replace(Regex("[^a-z0-9]"), ".")
            .replace(Regex("\\.+"), ".")
            .trim('.')
        
        return "com.example.$normalized"
    }
    
    private fun toClassName(name: String): String {
        return name.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotEmpty() }
            .joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}

fun main(args: Array<String>) {
    try {
        when (args.getOrNull(0)) {
            "create-project" -> {
                if (args.size < 2) {
                    println("Usage: archetype create-project <projectName> [java|kotlin]")
                    return
                }
                
                ArchetypeGenerator.generate(
                    templateType = "project",
                    language = args.getOrNull(2) ?: "java",
                    targetName = args[1]
                )
                
                println("Created project: ${args[1]}")
            }
            
            "create-module" -> {
                if (args.size < 2) {
                    println("Usage: archetype create-module <moduleName> [java|kotlin] [packageName]")
                    return
                }
                
                ArchetypeGenerator.generate(
                    templateType = "module",
                    language = args.getOrNull(2) ?: "java",
                    targetName = args[1],
                    packageName = args.getOrNull(3)
                )
                
                println("Created module: ${args[1]}")
            }
            
            else -> {
                println("""
                    Archetype Generator
                    
                    Usage:
                      archetype create-project <projectName> [java|kotlin]
                      archetype create-module <moduleName> [java|kotlin] [packageName]
                    
                    Examples:
                      archetype create-project my-app kotlin
                      archetype create-module api java com.example.api
                """.trimIndent())
            }
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        System.exit(1)
    }
}