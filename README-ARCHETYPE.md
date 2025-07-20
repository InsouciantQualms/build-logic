# Gradle Archetype Generator

A simple, template-based project generator for Gradle projects, similar to Maven archetypes.

## Overview

The archetype system uses a clear directory structure with visible templates that can be easily edited and understood. Templates are organized by type (project/module) and language (java/kotlin).

## Template Structure

```
archetype/
├── project/           # Templates for new projects
│   ├── java/         # Java project template
│   └── kotlin/       # Kotlin project template
├── module/           # Templates for new modules
│   ├── java/         # Java module template
│   └── kotlin/       # Kotlin module template
├── ArchetypeGenerator.kt  # Minimal template processor
└── archetype.gradle.kts   # Gradle script to run generator
```

## Installation

1. Build the build-logic project:
   ```bash
   cd build-logic
   ./gradlew jar
   ```

## Usage

### Creating a New Project

```bash
# Navigate to where you want the project
cd ~/my-projects

# Create a Java project
./gradlew -b /path/to/build-logic/archetype/archetype.gradle.kts createProject -PprojectName=my-app

# Create a Kotlin project
./gradlew -b /path/to/build-logic/archetype/archetype.gradle.kts createProject -PprojectName=my-app -Planguage=kotlin
```

### Creating a New Module

```bash
# From your project root
cd my-app

# Create a Java module
./gradlew -b /path/to/build-logic/archetype/archetype.gradle.kts createModule -PmoduleName=api

# Create a Kotlin module with custom package
./gradlew -b /path/to/build-logic/archetype/archetype.gradle.kts createModule -PmoduleName=domain -Planguage=kotlin -PpackageName=com.mycompany.domain
```

### Parameters

#### createProject
- `projectName` (required): Name of the new project
- `language` (optional): Programming language - `java` or `kotlin` (default: java)

#### createModule
- `moduleName` (required): Name of the new module
- `language` (optional): Programming language - `java` or `kotlin` (default: java)
- `packageName` (optional): Base package name for the module (default: derived from module name)

## Template Placeholders

The templates use the following placeholders that are replaced during generation:

- `{{projectName}}` - The project or module name
- `{{packageName}}` - The Java/Kotlin package name
- `{{packagePath}}` - The package name as a file path (com/example/app)
- `{{className}}` - The main class name derived from project/module name

## Customizing Templates

To customize the templates for your organization:

1. Navigate to `build-logic/archetype/`
2. Edit the template files directly
3. Add new files as needed
4. The generator will copy and process all files in the template directory

For example, to change the default test framework:
- Edit `module/java/src/test/java/{{packagePath}}/{{className}}Test.java`
- Replace JUnit imports and assertions with your preferred framework

## Features

- **Convention Plugins**: All generated projects/modules use the precompiled script plugins from build-logic
- **Version Catalog**: Projects are configured to use the centralized version catalog
- **Language Support**: Both Java and Kotlin are supported
- **Test Setup**: Each module includes a minimal test that proves the build works
- **Gradle Installation**: Assumes Gradle is already installed on the system
- **Package Naming**: Follows the pattern `dev.iq.${projectName}` by default

## How It Works

1. The `archetype.gradle.kts` script provides a minimal Gradle interface
2. `ArchetypeGenerator.kt` is a simple template processor that:
   - Copies the appropriate template directory
   - Replaces placeholders in file names and contents
   - Updates parent settings.gradle.kts for new modules
3. Templates are plain file structures that show exactly what will be generated

## Notes

- Generated projects reference `build-logic` as an included build
- Projects assume Gradle is already installed on the system
- The archetype generator runs from your current directory