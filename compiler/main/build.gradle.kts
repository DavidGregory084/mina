plugins {
    `java-library`
    `java-project-convention`
    alias(libs.plugins.gradleBuildInfo)
}

dependencies {
    // Concurrent Streams
    implementation(libs.reactorCore)

    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Trees
    implementation(project(":compiler:syntax"))

    // Parser
    implementation(project(":compiler:parser"))

    // Renamer
    implementation(project(":compiler:renamer"))

    // Typechecker
    implementation(project(":compiler:typechecker"))

    // JVM Bytecode Generation
    implementation(project(":compiler:jvm"))

    // Graph Data Structures
    implementation(libs.jgrapht)
    implementation(libs.jgraphtIo)
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}
