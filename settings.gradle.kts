dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("antlr", "4.10.1")
            version("eclipse-collections", "11.0.0")
            version("guice", "5.1.0")
            version("slf4j", "1.7.36")

            library("eclipse-collections-api", "org.eclipse.collections", "eclipse-collections-api").versionRef("eclipse-collections")
            library("eclipse-collections", "org.eclipse.collections", "eclipse-collections").versionRef("eclipse-collections")
            bundle("eclipse-collections", listOf("eclipse-collections-api", "eclipse-collections"))

            library("antlr", "org.antlr", "antlr4").versionRef("antlr")

            library("guice", "com.google.inject", "guice").versionRef("guice")

            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("jul-to-slf4j", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")
            bundle("slf4j", listOf("slf4j-api", "jul-to-slf4j"))
        }
    }
}

rootProject.name = "mina"

include("gradle-plugin")
project(":gradle-plugin").name = "mina-gradle-plugin"

// Common data structures
include("compiler:common")

// Frontend
include("compiler:syntax")
include("compiler:parser")

// Optimizer & code generation
include("compiler:intermediate")
include("compiler:jvm")

// Language Server
include("lang-server")
