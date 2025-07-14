/*
 * Insouciant Qualms © 2024 by Sascha Goldsmith is licensed under CC BY 4.0.
 * To view a copy of this license, visit https://creativecommons.org/licenses/by/4.0.
 * To reach the creator, visit https://www.linkedin.com/in/saschagoldsmith.
 */

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
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
    logger.lifecycle("Downloading $uri ... ")
    uri.toURL().openStream().use { input ->
        dest.outputStream().use { output ->
            output.write(input.readBytes())
        }
    }
    logger.lifecycle(" done! (${String.format("%,d", dest.length())} bytes).")
}

/**
 * Helper function to resolve resources in a library-for-libs notation format where the
 * actual LibraryForLibs is missing (for example, contention plugins).
 *
 * The name parameter is in the format "libs.dependency", "libs.versions.foo", etc.
 */
fun Project.resolve(name: String): String {
    val pieces = name.split(".")
    require(pieces.size > 1) { "Invalid name format: $name" }
    val catalog = project.extensions.getByType<VersionCatalogsExtension>().named(pieces[0])
    return when (pieces[1]) {
        "plugins" -> resolvePlugin(concatenate(pieces, 2), catalog)
        "versions" -> resolveVersion(concatenate(pieces, 2), catalog)
        "bundles" -> resolveBundle(concatenate(pieces, 2), catalog)
        else -> resolveLibrary(concatenate(pieces, 1), catalog)
    }
}

private fun resolvePlugin(name: String, cat: VersionCatalog): String = resolve(name) {
    cat.findPlugin(name).map { p -> p.get() }.map { p -> p.toString() }
}

private fun resolveVersion(name: String, cat: VersionCatalog): String = resolve(name) {
    cat.findVersion(name).map { v -> v.requiredVersion }
}

private fun resolveLibrary(name: String, cat: VersionCatalog): String = resolve(name) {
    cat.findLibrary(name).map { p -> p.get() }.map { l -> l.toString() }
}

private fun resolveBundle(name: String, cat: VersionCatalog): String = resolve(name) {
    cat.findBundle(name).map { p -> p.get() }.map { b -> b.toString() }
}

private fun <T> resolve(name: String, fx: () -> java.util.Optional<T>): T = fx.invoke().orElseThrow { toException(name) }

private fun concatenate(pieces: List<String>, index: Int): String = pieces.slice(index until pieces.size).joinToString(".")

private fun toException(name: String): IllegalStateException = java.lang.IllegalStateException("Dependency not found '$name'")
