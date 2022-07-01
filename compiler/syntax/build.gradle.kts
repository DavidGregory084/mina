plugins {
    `java-library`
    `java-project-convention`
    `java-protobuf-convention`
}

dependencies {
    // Immutable Collections
    implementation(libs.bundles.eclipseCollections)

    // Common Definitions
    implementation(project(":compiler:common"))
}
