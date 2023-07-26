plugins {
    application
    `java-project-convention`
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Unix Socket Support
    // We must use a pom type dependency here, because Gradle's platform()
    // doesn't actually resolve the transitive dependencies so they would
    // have to be added separately
    implementation("${libs.junixSocket.get()}@pom") { isTransitive = true }

    // Language Server Protocol
    implementation(libs.lsp4j)

    // Build Server Protocol
    implementation(libs.bsp4j)

    // Platform-specific Directories
    implementation(libs.directories)

    // Failable Streams
    implementation(libs.apacheCommonsLang)

    // Compiler Main
    implementation(project(":compiler:mina-compiler-main"))

    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly(libs.logback)
}

application { mainClass.set("org.mina_lang.langserver.MinaServerLauncher") }
