plugins {
    `java-library`
    `java-project-convention`
    `java-protobuf-convention`
}

dependencies {
    // Immutable Collections
    implementation(libs.bundles.eclipse.collections)

    // Common Definitions
    implementation(project(":compiler:common"))
}
