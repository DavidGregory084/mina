plugins {
    `java-library`
    `java-project-convention`
    alias(libs.plugins.gradleBuildInfo)
}

dependencies {
    // Concurrent Streams
    api(libs.reactorCore)
    testImplementation(libs.reactorTest)

    // Sytax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Parser
    implementation(project(":compiler:mina-compiler-parser"))

    // CharStream appears in the public API
    api(libs.antlrRuntime)

    // Renamer
    implementation(project(":compiler:mina-compiler-renamer"))

    // Typechecker
    implementation(project(":compiler:mina-compiler-typechecker"))

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
