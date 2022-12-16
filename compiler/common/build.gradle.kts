plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Immutable collections
    implementation(libs.bundles.eclipseCollections)

    // Pretty printing
    implementation(libs.prettier4j)
}