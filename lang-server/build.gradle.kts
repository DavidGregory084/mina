plugins {
    application
    `java-project-convention`
    id("com.opencastsoftware.gradle.buildinfo") version "0.1.0"
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Unix Socket Support
    implementation("${libs.junixSocket.get()}@pom") {
        isTransitive = true
    }

    // Language Server Protocol
    implementation(libs.lsp4j)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Trees
    implementation(project(":compiler:syntax"))

    // Parser
    implementation(project(":compiler:parser"))

    // Renamer
    implementation(project(":compiler:renamer"))

    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly(libs.logback)
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}

application {
    mainClass.set("org.mina_lang.langserver.MinaServerLauncher")
}
