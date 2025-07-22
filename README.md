# Gradle Archetype Generator

A simple, bash-based project generator for Gradle projects, similar to Maven archetypes.

## Overview

The archetype system uses a clear directory structure with visible templates that can be easily edited and understood. Projects are language-agnostic and only modules have language distinctions. A simple bash script handles the generation process.

## Template Structure

```
archetype/
├── project/          # Single template for new projects
│   ├── .gitignore   # Standard gitignore
│   └── settings.gradle.kts # References ../build-logic
├── module/          # Templates for new modules
│   ├── java/        # Java module template
│   └── kotlin/      # Kotlin module template
└── generate*        # Bash script to generate projects/modules
```

## Installation

The archetype generator is ready to use - no build step required. Just make sure the script is executable:

```bash
chmod +x /path/to/build-logic/archetype/generate
```

## Usage

### Creating a New Project

```bash
# Navigate to where you want the project
cd ~/my-projects

# Create a project (language-agnostic)
/path/to/build-logic/archetype/generate create-project my-app
```

### Creating a New Module

```bash
# From your project root
cd my-app

# Create a Java module
/path/to/build-logic/archetype/generate create-module app java

# Create a Kotlin module with custom package
/path/to/build-logic/archetype/generate create-module domain kotlin com.mycompany.domain
```

### Command Syntax

#### create-project
```bash
./generate create-project <projectName>
```
- `projectName` (required): Name of the new project

#### create-module  
```bash
./generate create-module <moduleName> [java|kotlin] [packageName]
```
- `moduleName` (required): Name of the new module
- `language` (optional): Programming language - `java` or `kotlin` (default: java) 
- `packageName` (optional): Base package name for the module (default: derived from module name)

## Template Placeholders

The templates use the following placeholders that are replaced during generation:

- `{{projectName}}` - The project or module name
- `{{packageName}}` - The Java/Kotlin package name (e.g., `com.example.myapp`)
- `{{packagePath}}` - The package name as a file path (e.g., `com/example/myapp`)
- `{{className}}` - The main class name derived from project/module name (e.g., `MyApp`)

## Generated Project Structure

### Project (Language-agnostic)
```
my-app/
├── .gitignore                     # Standard exclusions
└── settings.gradle.kts            # References ../build-logic, version catalog
```

### After adding modules
```
my-app/
├── .gitignore
├── settings.gradle.kts            # Updated with include("app"), include("api")
├── app/                           # Java module
│   ├── build.gradle.kts          # Uses java-common plugin
│   └── src/
│       ├── main/java/com/example/app/
│       │   └── App.java
│       └── test/java/com/example/app/
│           └── AppTest.java       # JUnit test
└── api/                          # Kotlin module  
    ├── build.gradle.kts          # Uses kotlin-common plugin
    └── src/
        ├── main/kotlin/com/example/api/
        │   └── Api.kt
        └── test/kotlin/com/example/api/
            └── ApiTest.kt        # Kotest test
```

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

- **Simple Bash Implementation**: No complex build dependencies
- **Convention Plugins**: All generated modules use the precompiled script plugins from build-logic
- **Relative Build-Logic Reference**: Projects reference the build-logic via `../build-logic` (no copying)
- **Version Catalog**: Projects are configured to use the centralized version catalog  
- **Language Support**: Modules support both Java and Kotlin
- **Test Setup**: Each module includes a minimal unit test that proves the build works
- **Package Naming**: Follows the pattern `com.example.${moduleName}` by default
- **Directory Independence**: Can be run from any directory to create projects/modules
- **Automatic Settings Update**: Module creation automatically adds `include()` to settings.gradle.kts

## How It Works

1. The bash script `generate` provides a simple command-line interface
2. **Project creation**: Copies minimal template (just .gitignore and settings.gradle.kts)
3. **Module creation**: Copies from `archetype/module/{language}/` and updates parent settings
4. Placeholders in file names and contents are replaced using `sed`
5. Projects reference build-logic via relative path, no copying required

## Testing Your Generated Projects

### Basic Usage
```bash
# Create project
./archetype/generate create-project my-app
cd my-app

# Add a module
../build-logic/archetype/generate create-module app java

# Build everything
../build-logic/gradlew build
```

### Advanced Usage - Nested Modules
```bash
# Create project
./archetype/generate create-project my-company-project
cd my-company-project

# Add top-level modules
../build-logic/archetype/generate create-module backend kotlin
../build-logic/archetype/generate create-module frontend java

# Create nested modules
mkdir -p backend/services
cd backend/services
../../../build-logic/archetype/generate create-module user-service kotlin com.mycompany.users
../../../build-logic/archetype/generate create-module auth-service kotlin com.mycompany.auth

# The archetype generator will automatically:
# - Find the root settings.gradle.kts file
# - Add correct include paths like include(":backend:services:user-service")
# - Calculate proper relative paths to build-logic

# Build everything from root
cd ../..
../build-logic/gradlew build
```

This should:
1. Download dependencies
2. Compile the main and test sources  
3. Run the unit tests
4. Build successfully

## Notes

- Generated projects reference build-logic via relative path (`../build-logic`)
- Projects need modules to be useful - the project template is just the foundation
- The archetype generator treats your current directory as the target location
- Package names are automatically derived from module names following Java naming conventions
- Module creation automatically updates the parent `settings.gradle.kts` file