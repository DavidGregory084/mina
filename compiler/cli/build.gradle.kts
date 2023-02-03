plugins {
    application
    `java-project-convention`
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Compiler Main
    implementation(project(":compiler:main"))

    // Logging
    implementation(libs.slf4jApi)
    implementation(libs.logback)

    // Diagnostic Reporting
    implementation(libs.jansi)

    // Failable Streams
    implementation(libs.apacheCommonsLang)
}

application {
    applicationName = "minac"
    mainClass.set("org.mina_lang.cli.MinaCommandLine")
}
