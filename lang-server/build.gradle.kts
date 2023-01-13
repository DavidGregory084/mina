plugins {
    application
    `java-project-convention`
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

    // Pretty Printing
    implementation(libs.prettier4j)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Trees
    implementation(project(":compiler:syntax"))

    // Parser
    implementation(project(":compiler:parser"))

    // Compiler Main
    implementation(project(":compiler:main"))

    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly(libs.logback)
}

application {
    mainClass.set("org.mina_lang.langserver.MinaServerLauncher")
}
