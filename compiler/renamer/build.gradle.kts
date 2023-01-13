plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Tree Definitions
    implementation(project(":compiler:syntax"))

    // Graph Data Structures
    implementation(libs.jgrapht)
    implementation(libs.jgraphtIo)
}