plugins {
    `java-library`
    id("java-project-convention")
    alias(libs.plugins.gradleBuildInfo)
}

dependencies {
    // Concurrent Streams
    api(libs.reactorCore)
    testImplementation(libs.reactorTest)

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Intermediate Language Definitions
    api(project(":compiler:mina-compiler-intermediate"))

    // Parser
    implementation(project(":compiler:mina-compiler-parser"))

    // CharStream appears in the public API
    api(libs.antlrRuntime)

    // Renamer
    implementation(project(":compiler:mina-compiler-renamer"))

    // Typechecker
    implementation(project(":compiler:mina-compiler-typechecker"))

    // Optimiser
    implementation(project(":compiler:mina-compiler-optimiser"))

    // JVM Bytecode Generation
    implementation(project(":compiler:mina-compiler-jvm"))

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
