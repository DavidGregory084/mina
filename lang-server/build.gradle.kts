plugins {
    application
    `java-project-convention`
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Unix Socket Support
    implementation(platform(libs.junixSocket.get()))

    // Language Server Protocol
    implementation(libs.lsp4j)

    // Compiler Main
    implementation(project(":compiler:main"))

    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly(libs.logback)
}

application {
    mainClass.set("org.mina_lang.langserver.MinaServerLauncher")
}
