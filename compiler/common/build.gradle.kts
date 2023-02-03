plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Immutable collections
    api(libs.bundles.eclipseCollections)

    // Pretty printing
    implementation(libs.prettier4j)

    // Diagnostic types
    api(libs.yvette)
}