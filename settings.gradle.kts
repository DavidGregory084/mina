rootProject.name = "mina"

// Inexplicably, the recommended way to configure artifact names in Gradle
// is to set the actual project name.
// This means you have to override the names that are inferred from your source folder
// structure in order to get the jar file names that you want.
// See https://github.com/gradle/gradle/issues/11299 for more details.

include("gradle-plugin")
project(":gradle-plugin").name = "mina-gradle-plugin"

// Common data structures
include("compiler:common")
project(":compiler:common").name = "mina-compiler-common"

// Frontend
include("compiler:syntax")
project(":compiler:syntax").name = "mina-compiler-syntax"
include("compiler:parser")
project(":compiler:parser").name = "mina-compiler-parser"
include("compiler:renamer")
project(":compiler:renamer").name = "mina-compiler-renamer"
include("compiler:typechecker")
project(":compiler:typechecker").name = "mina-compiler-typechecker"

// Compiler main
include("compiler:main")
project(":compiler:main").name = "mina-compiler-main"

// Command line interface
include("compiler:cli")
project(":compiler:cli").name = "mina-compiler"

// Optimizer & code generation
include("compiler:intermediate")
project(":compiler:intermediate").name = "mina-compiler-intermediate"
include("compiler:jvm")
project(":compiler:jvm").name = "mina-compiler-jvm"

// Language server
include("lang-server")
project(":lang-server").name = "mina-lang-server"

// Runtime library
include("runtime")
project(":runtime").name = "mina-runtime"

// VS Code plugin
include("vscode-plugin")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}