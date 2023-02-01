plugins {
    application
    `java-project-convention`
}

repositories {
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Parser
    implementation(project(":compiler:parser"))

    // Compiler Main
    implementation(project(":compiler:main"))

    // Logging
    implementation(libs.slf4jApi)
    implementation(libs.logback)

    // Diagnostic Reporting
    implementation(libs.yvette)
    implementation(libs.jansi)

    // Failable Streams
    implementation(libs.apacheCommonsLang)
}

application {
    applicationName = "minac"
    mainClass.set("org.mina_lang.cli.MinaCommandLine")
}
