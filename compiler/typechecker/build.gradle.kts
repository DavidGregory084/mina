plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Pretty Printing
    implementation(libs.prettier4j)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Tree Definitions
    implementation(project(":compiler:syntax"))
}