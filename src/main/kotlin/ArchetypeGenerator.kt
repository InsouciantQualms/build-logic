import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

class ArchetypeGenerator {
    
    enum class ProjectType {
        PROJECT, MODULE
    }
    
    enum class Language {
        JAVA, KOTLIN
    }
    
    data class GeneratorConfig(
        val projectName: String,
        val projectType: ProjectType,
        val language: Language,
        val targetPath: Path = Paths.get("."),
        val packageName: String? = null
    )
    
    fun generate(config: GeneratorConfig) {
        when (config.projectType) {
            ProjectType.PROJECT -> generateProject(config)
            ProjectType.MODULE -> generateModule(config)
        }
    }
    
    private fun generateProject(config: GeneratorConfig) {
        val projectPath = config.targetPath.resolve(config.projectName)
        if (projectPath.exists()) {
            throw IllegalArgumentException("Project ${config.projectName} already exists at ${projectPath.toAbsolutePath()}")
        }
        
        projectPath.createDirectories()
        
        generateProjectSettingsFile(projectPath, config.projectName)
        generateProjectBuildFile(projectPath)
        generateGradleWrapper(projectPath)
        generateGitignore(projectPath)
        
        val modulePath = projectPath.resolve("app")
        generateModule(config.copy(
            projectType = ProjectType.MODULE,
            targetPath = projectPath,
            projectName = "app"
        ))
    }
    
    private fun generateModule(config: GeneratorConfig) {
        val modulePath = config.targetPath.resolve(config.projectName)
        if (modulePath.exists()) {
            throw IllegalArgumentException("Module ${config.projectName} already exists at ${modulePath.toAbsolutePath()}")
        }
        
        modulePath.createDirectories()
        
        val packageName = config.packageName ?: derivePackageName(config.projectName)
        val packagePath = packageName.replace('.', '/')
        
        generateModuleBuildFile(modulePath, config.language)
        
        val srcMainPath = modulePath.resolve("src/main/${config.language.name.lowercase()}/$packagePath")
        val srcTestPath = modulePath.resolve("src/test/${config.language.name.lowercase()}/$packagePath")
        
        srcMainPath.createDirectories()
        srcTestPath.createDirectories()
        
        when (config.language) {
            Language.JAVA -> {
                generateJavaMainClass(srcMainPath, packageName, config.projectName)
                generateJavaTestClass(srcTestPath, packageName, config.projectName)
            }
            Language.KOTLIN -> {
                generateKotlinMainClass(srcMainPath, packageName, config.projectName)
                generateKotlinTestClass(srcTestPath, packageName, config.projectName)
            }
        }
        
        if (config.projectType == ProjectType.MODULE && config.targetPath != Paths.get(".")) {
            updateParentSettings(config.targetPath, config.projectName)
        }
    }
    
    private fun generateProjectSettingsFile(projectPath: Path, projectName: String) {
        val content = """
            rootProject.name = "$projectName"
            
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
                includeBuild("build-logic")
            }
            
            dependencyResolutionManagement {
                @Suppress("UnstableApiUsage")
                repositories {
                    mavenCentral()
                }
                
                @Suppress("UnstableApiUsage")
                versionCatalogs {
                    create("libs") {
                        from("dev.iq:libs:+")
                    }
                }
            }
            
            include(":app")
        """.trimIndent()
        
        projectPath.resolve("settings.gradle.kts").writeText(content)
    }
    
    private fun generateProjectBuildFile(projectPath: Path) {
        val content = """
            plugins {
                id("base")
            }
            
            allprojects {
                group = "dev.iq.${'$'}{rootProject.name}"
                version = "0.1.0-SNAPSHOT"
            }
        """.trimIndent()
        
        projectPath.resolve("build.gradle.kts").writeText(content)
    }
    
    private fun generateModuleBuildFile(modulePath: Path, language: Language) {
        val plugin = when (language) {
            Language.JAVA -> "java-common"
            Language.KOTLIN -> "kotlin-common"
        }
        
        val content = """
            plugins {
                id("$plugin")
            }
            
            dependencies {
                // Add your dependencies here
            }
        """.trimIndent()
        
        modulePath.resolve("build.gradle.kts").writeText(content)
    }
    
    private fun generateJavaMainClass(srcPath: Path, packageName: String, projectName: String) {
        val className = toClassName(projectName)
        val content = """
            package $packageName;
            
            public class $className {
                public String greet(String name) {
                    return "Hello, " + name + "!";
                }
                
                public static void main(String[] args) {
                    $className app = new $className();
                    System.out.println(app.greet("World"));
                }
            }
        """.trimIndent()
        
        srcPath.resolve("$className.java").writeText(content)
    }
    
    private fun generateJavaTestClass(testPath: Path, packageName: String, projectName: String) {
        val className = toClassName(projectName)
        val content = """
            package $packageName;
            
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            
            class ${className}Test {
                @Test
                void testGreet() {
                    $className app = new $className();
                    assertEquals("Hello, Test!", app.greet("Test"));
                }
            }
        """.trimIndent()
        
        testPath.resolve("${className}Test.java").writeText(content)
    }
    
    private fun generateKotlinMainClass(srcPath: Path, packageName: String, projectName: String) {
        val className = toClassName(projectName)
        val content = """
            package $packageName
            
            class $className {
                fun greet(name: String): String {
                    return "Hello, ${'$'}name!"
                }
            }
            
            fun main() {
                val app = $className()
                println(app.greet("World"))
            }
        """.trimIndent()
        
        srcPath.resolve("$className.kt").writeText(content)
    }
    
    private fun generateKotlinTestClass(testPath: Path, packageName: String, projectName: String) {
        val className = toClassName(projectName)
        val content = """
            package $packageName
            
            import io.kotest.core.spec.style.StringSpec
            import io.kotest.matchers.shouldBe
            
            class ${className}Test : StringSpec({
                "greet should return greeting message" {
                    val app = $className()
                    app.greet("Test") shouldBe "Hello, Test!"
                }
            })
        """.trimIndent()
        
        testPath.resolve("${className}Test.kt").writeText(content)
    }
    
    private fun generateGradleWrapper(projectPath: Path) {
        val wrapperDir = projectPath.resolve("gradle/wrapper")
        wrapperDir.createDirectories()
        
        projectPath.resolve("gradlew").writeText(getGradlewScript())
        projectPath.resolve("gradlew.bat").writeText(getGradlewBatScript())
        
        File(projectPath.resolve("gradlew").toString()).setExecutable(true)
        
        // Copy gradle-wrapper files from the current build-logic project
        val currentWrapperDir = Paths.get("gradle/wrapper")
        if (currentWrapperDir.resolve("gradle-wrapper.jar").exists()) {
            try {
                Files.copy(
                    currentWrapperDir.resolve("gradle-wrapper.jar"),
                    wrapperDir.resolve("gradle-wrapper.jar"),
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (e: Exception) {
                // If we can't copy the jar, create a minimal placeholder
                wrapperDir.resolve("gradle-wrapper.jar").writeText("")
            }
        }
        
        // Create gradle-wrapper.properties
        wrapperDir.resolve("gradle-wrapper.properties").writeText("""
            distributionBase=GRADLE_USER_HOME
            distributionPath=wrapper/dists
            distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
            networkTimeout=10000
            validateDistributionUrl=true
            zipStoreBase=GRADLE_USER_HOME
            zipStorePath=wrapper/dists
        """.trimIndent())
    }
    
    private fun generateGitignore(projectPath: Path) {
        val content = """
            .gradle/
            build/
            .idea/
            *.iml
            .DS_Store
            out/
            bin/
            .classpath
            .project
            .settings/
            .vscode/
        """.trimIndent()
        
        projectPath.resolve(".gitignore").writeText(content)
    }
    
    private fun updateParentSettings(parentPath: Path, moduleName: String) {
        val settingsFile = parentPath.resolve("settings.gradle.kts")
        if (settingsFile.exists()) {
            val currentContent = settingsFile.toFile().readText()
            if (!currentContent.contains("include(\":$moduleName\")")) {
                val updatedContent = currentContent + "\ninclude(\":$moduleName\")"
                settingsFile.writeText(updatedContent)
            }
        }
    }
    
    private fun derivePackageName(projectName: String): String {
        val sanitized = projectName.lowercase()
            .replace(Regex("[^a-z0-9]"), ".")
            .replace(Regex("\\.+"), ".")
            .trim('.')
        return "dev.iq.$sanitized"
    }
    
    private fun toClassName(projectName: String): String {
        return projectName.split(Regex("[^a-zA-Z0-9]"))
            .filter { it.isNotEmpty() }
            .joinToString("") { it.capitalize() }
    }
    
    private fun getGradlewScript(): String = """
        #!/bin/sh
        
        ##############################################################################
        #
        #   Gradle start up script for POSIX generated by Gradle.
        #
        ##############################################################################
        
        # Attempt to set APP_HOME
        # Resolve links: ${'$'}0 may be a link
        app_path=${'$'}0
        
        # Need this for daisy-chained symlinks.
        while
            APP_HOME=${'$'}{app_path%"${'$'}{app_path##*/}"}  # leaves a trailing /; empty if no leading path
            [ -h "${'$'}app_path" ]
        do
            ls=${'$'}( ls -ld "${'$'}app_path" )
            link=${'$'}{ls#*' -> '}
            case ${'$'}link in             #(
              /*)   app_path=${'$'}link ;; #(
              *)    app_path=${'$'}APP_HOME${'$'}link ;;
            esac
        done
        
        APP_BASE_NAME=${'$'}{0##*/}
        APP_HOME=${'$'}( cd "${'$'}{APP_HOME:-./}" && pwd -P ) || exit
        
        # Use the maximum available, or set MAX_FD != -1 to use that value.
        MAX_FD=maximum
        
        # OS specific support (must be 'true' or 'false').
        cygwin=false
        msys=false
        darwin=false
        nonstop=false
        case "${'$'}( uname )" in                #(
          CYGWIN* )         cygwin=true  ;;     #(
          Darwin* )         darwin=true  ;;     #(
          MSYS* | MINGW* )  msys=true    ;;     #(
          NONSTOP* )        nonstop=true ;;
        esac
        
        CLASSPATH=${'$'}APP_HOME/gradle/wrapper/gradle-wrapper.jar
        
        # Determine the Java command to use to start the JVM.
        if [ -n "${'$'}JAVA_HOME" ] ; then
            if [ -x "${'$'}JAVA_HOME/jre/sh/java" ] ; then
                # IBM's JDK on AIX uses strange locations for the executables
                JAVACMD=${'$'}JAVA_HOME/jre/sh/java
            else
                JAVACMD=${'$'}JAVA_HOME/bin/java
            fi
            if [ ! -x "${'$'}JAVACMD" ] ; then
                die "ERROR: JAVA_HOME is set to an invalid directory: ${'$'}JAVA_HOME
        
        Please set the JAVA_HOME variable in your environment to match the
        location of your Java installation."
            fi
        else
            JAVACMD=java
            if ! command -v java >/dev/null 2>&1
            then
                die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
        
        Please set the JAVA_HOME variable in your environment to match the
        location of your Java installation."
            fi
        fi
        
        # Increase the maximum file descriptors if we can.
        if ! "${'$'}cygwin" && ! "${'$'}darwin" && ! "${'$'}nonstop" ; then
            case ${'$'}MAX_FD in #(
              max*)
                MAX_FD=${'$'}( ulimit -H -n ) ||
                    warn "Could not query maximum file descriptor limit"
            esac
            case ${'$'}MAX_FD in  #(
              '' | soft) :;; #(
              *)
                ulimit -n "${'$'}MAX_FD" ||
                    warn "Could not set maximum file descriptor limit to ${'$'}MAX_FD"
            esac
        fi
        
        # Collect all arguments for the java command, stacking in reverse order:
        #   * args from the command line
        #   * the main class name
        #   * -classpath
        #   * -D...appname settings
        #   * --module-path (only if needed)
        #   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.
        
        # For Cygwin or MSYS, switch paths to Windows format before running java
        if "${'$'}cygwin" || "${'$'}msys" ; then
            APP_HOME=${'$'}( cygpath --path --mixed "${'$'}APP_HOME" )
            CLASSPATH=${'$'}( cygpath --path --mixed "${'$'}CLASSPATH" )
        
            JAVACMD=${'$'}( cygpath --unix "${'$'}JAVACMD" )
        
            # Now convert the arguments - kludge to limit ourselves to /bin/sh
            for arg do
                if
                    case ${'$'}arg in                                #(
                      -*)   false ;;                            # don't mess with options #(
                      /?*)  t=${'$'}{arg#/} t=/${'$'}{t%%/*}              # looks like a POSIX filepath
                            [ -e "${'$'}t" ] ;;                      #(
                      *)    false ;;
                    esac
                then
                    arg=${'$'}( cygpath --path --ignore --mixed "${'$'}arg" )
                fi
                # Roll the args list around exactly as many times as the number of
                # args, so each arg winds up back in the position where it started, but
                # possibly modified.
                #
                # NB: a `for` loop captures its iteration list before it begins, so
                # changing the positional parameters here affects neither the number of
                # iterations, nor the values presented in `arg`.
                shift                   # remove old arg
                set -- "${'$'}@" "${'$'}arg"      # push replacement arg
            done
        fi
        
        DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
        
        # Collect all arguments for the java command;
        #   * ${'$'}DEFAULT_JVM_OPTS, ${'$'}JAVA_OPTS, and ${'$'}GRADLE_OPTS can contain fragments of
        #     shell script including quotes and variable substitutions, so put them in
        #     double quotes to make sure that they get re-expanded; and
        #   * put everything else in single quotes, so that it's not re-expanded.
        
        set -- \
                "-Dorg.gradle.appname=${'$'}APP_BASE_NAME" \
                -classpath "${'$'}CLASSPATH" \
                org.gradle.wrapper.GradleWrapperMain \
                "${'$'}@"
        
        # Stop when "xargs" is not available.
        if ! command -v xargs >/dev/null 2>&1
        then
            die "xargs is not available"
        fi
        
        # Use "xargs" to parse quoted args.
        #
        # With -n1 it outputs one arg per line, with the quotes and backslashes removed.
        #
        # In Bash we could simply go:
        #
        #   readarray ARGS < <( xargs -n1 <<<"${'$'}var" ) &&
        #   set -- "${'$'}{ARGS[@]}" "${'$'}@"
        #
        # but POSIX shell has neither arrays nor command substitution, so instead we
        # post-process each arg (as a line of input to sed) to backslash-escape any
        # character that might be a shell metacharacter, then use eval to reverse
        # that process (while maintaining the separation between arguments), and wrap
        # the whole thing up as a single "set" statement.
        #
        # This will of course break if any of these variables contains a newline or
        # an unmatched quote.
        #
        
        eval "set -- ${'$'}(
                printf '%s\n' "${'$'}DEFAULT_JVM_OPTS ${'$'}JAVA_OPTS ${'$'}GRADLE_OPTS" |
                xargs -n1 |
                sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
                tr '\n' ' '
            )" '"${'$'}@"'
        
        exec "${'$'}JAVACMD" "${'$'}@"
    """.trimIndent()
    
    private fun getGradlewBatScript(): String = """
        @rem
        @rem Gradle start up script for Windows
        @rem
        
        @if "%DEBUG%"=="" @echo off
        @rem ##########################################################################
        @rem
        @rem  Gradle startup script for Windows
        @rem
        @rem ##########################################################################
        
        @rem Set local scope for the variables with windows NT shell
        if "%OS%"=="Windows_NT" setlocal
        
        set DIRNAME=%~dp0
        if "%DIRNAME%"=="" set DIRNAME=.
        @rem This is normally unused
        set APP_BASE_NAME=%~n0
        set APP_HOME=%DIRNAME%
        
        @rem Resolve any "." and ".." in APP_HOME to make it shorter.
        for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi
        
        @rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
        set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
        
        @rem Find java.exe
        if defined JAVA_HOME goto findJavaFromJavaHome
        
        set JAVA_EXE=java.exe
        %JAVA_EXE% -version >NUL 2>&1
        if %ERRORLEVEL% equ 0 goto execute
        
        echo.
        echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
        echo.
        echo Please set the JAVA_HOME variable in your environment to match the
        echo location of your Java installation.
        
        goto fail
        
        :findJavaFromJavaHome
        set JAVA_HOME=%JAVA_HOME:"=%
        set JAVA_EXE=%JAVA_HOME%/bin/java.exe
        
        if exist "%JAVA_EXE%" goto execute
        
        echo.
        echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
        echo.
        echo Please set the JAVA_HOME variable in your environment to match the
        echo location of your Java installation.
        
        goto fail
        
        :execute
        @rem Setup the command line
        
        set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
        
        
        @rem Execute Gradle
        "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
        
        :end
        @rem End local scope for the variables with windows NT shell
        if %ERRORLEVEL% equ 0 goto mainEnd
        
        :fail
        rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
        rem the _cmd.exe /c_ return code!
        set EXIT_CODE=%ERRORLEVEL%
        if %EXIT_CODE% equ 0 set EXIT_CODE=1
        if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
        exit /b %EXIT_CODE%
        
        :mainEnd
        if "%OS%"=="Windows_NT" endlocal
        
        :omega
    """.trimIndent()
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }
    
    val command = args[0].lowercase()
    
    when (command) {
        "create-project" -> handleCreateProject(args.drop(1))
        "create-module" -> handleCreateModule(args.drop(1))
        else -> {
            println("Unknown command: $command")
            printUsage()
        }
    }
}

fun handleCreateProject(args: List<String>) {
    if (args.isEmpty()) {
        println("Error: Project name is required")
        printUsage()
        return
    }
    
    val projectName = args[0]
    val language = if (args.size > 1) {
        when (args[1].lowercase()) {
            "java" -> ArchetypeGenerator.Language.JAVA
            "kotlin" -> ArchetypeGenerator.Language.KOTLIN
            else -> {
                println("Error: Invalid language '${args[1]}'. Must be 'java' or 'kotlin'")
                return
            }
        }
    } else {
        ArchetypeGenerator.Language.JAVA
    }
    
    val generator = ArchetypeGenerator()
    val config = ArchetypeGenerator.GeneratorConfig(
        projectName = projectName,
        projectType = ArchetypeGenerator.ProjectType.PROJECT,
        language = language
    )
    
    try {
        generator.generate(config)
        println("Successfully created $language project: $projectName")
    } catch (e: Exception) {
        println("Error creating project: ${e.message}")
    }
}

fun handleCreateModule(args: List<String>) {
    if (args.isEmpty()) {
        println("Error: Module name is required")
        printUsage()
        return
    }
    
    val moduleName = args[0]
    val language = if (args.size > 1) {
        when (args[1].lowercase()) {
            "java" -> ArchetypeGenerator.Language.JAVA
            "kotlin" -> ArchetypeGenerator.Language.KOTLIN
            else -> {
                println("Error: Invalid language '${args[1]}'. Must be 'java' or 'kotlin'")
                return
            }
        }
    } else {
        ArchetypeGenerator.Language.JAVA
    }
    
    val packageName = if (args.size > 2) args[2] else null
    
    val generator = ArchetypeGenerator()
    val config = ArchetypeGenerator.GeneratorConfig(
        projectName = moduleName,
        projectType = ArchetypeGenerator.ProjectType.MODULE,
        language = language,
        packageName = packageName
    )
    
    try {
        generator.generate(config)
        println("Successfully created $language module: $moduleName")
    } catch (e: Exception) {
        println("Error creating module: ${e.message}")
    }
}

fun printUsage() {
    println("""
        Usage:
          gradle archetype create-project <project-name> [java|kotlin]
          gradle archetype create-module <module-name> [java|kotlin] [package-name]
        
        Commands:
          create-project    Create a new Gradle project with build-logic support
          create-module     Create a new module in the current project
        
        Options:
          language          Programming language (java or kotlin). Default: java
          package-name      Base package name for the module (optional)
        
        Examples:
          gradle archetype create-project my-app kotlin
          gradle archetype create-module api java dev.iq.myapp.api
    """.trimIndent())
}