plugins {
    application
    `java-project-convention`
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)

    // Immutable Collectionrs
    implementation(libs.bundles.eclipseCollections)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Parser
    implementation(project(":compiler:parser"))

    // Compiler Main
    implementation(project(":compiler:main"))
}

application {
    mainClass.set("org.mina_lang.cli.MinaCommandLine")
}
