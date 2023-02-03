plugins {
    `java-library`
    `java-project-convention`
    alias(libs.plugins.gradleBuildInfo)
}

dependencies {
    // Concurrent Streams
    api(libs.reactorCore)

    // Sytax Tree Definitions
    api(project(":compiler:syntax"))

    // Parser
    implementation(project(":compiler:parser"))

    // CharStream appears in the public API
    api(libs.antlr)

    // Renamer
    implementation(project(":compiler:renamer"))

    // Typechecker
    implementation(project(":compiler:typechecker"))

    // JVM Bytecode Generation
    implementation(project(":compiler:jvm"))

    // Graph Data Structures
    implementation(libs.jgrapht)
    implementation(libs.jgraphtIo)

    // Logging
    implementation(libs.slf4jApi)
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}
