/*
 * Insouciant Qualms © 2024 by Sascha Goldsmith is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0.
 * To reach the creator, visit https://www.linkedin.com/in/saschagoldsmith.
 */

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI

/**
 * Custom task that downloads a resource to the filesystem.
 */
@Suppress("unused")
abstract class Download : DefaultTask() {

    /** URI to download. */
    @get:Input
    abstract val uri: Property<String>

    /** Destination for the download. */
    @get:OutputFile
    abstract val dest: Property<File>

    /**
     * Executes the download action during the build.
     */
    @TaskAction
    fun download() {
        project.download(URI(uri.get()), dest.get())
    }
}

/**
 * Helper function to download a file to the specified location.
 */
fun Project.download(uri: URI, dest: File) {
    logger.info("Downloading $uri ... ")
    uri.toURL().openStream().use { input ->
        dest.outputStream().use { output ->
            output.write(input.readBytes())
        }
    }
    logger.info(" done! (${String.format("%,d", dest.length())} bytes).")
}

/**
 * Copy a resource on the classpath to a destination file.
 */
fun Project.copyFromClasspath(resourcePath: String, dest: File) {
    logger.info("Copying classpath resource '$resourcePath' to ${dest.absolutePath} ... ")
    dest.parentFile?.mkdirs()
    val resourceStream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
        ?: javaClass.classLoader.getResourceAsStream(resourcePath)
        ?: ClassLoader.getSystemResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Resource not found on classpath: $resourcePath")
    resourceStream.use { input ->
        dest.outputStream().use { output -> output.write(input.readBytes()) }
    }
    logger.info(" done! (${String.format("%,d", dest.length())} bytes).")
}
