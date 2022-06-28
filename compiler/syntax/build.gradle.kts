plugins {
    `java-library`
    `java-project-convention`
    `java-protobuf-convention`
}

val eclipseCollectionsVersion = "11.0.0"

dependencies {
    // Immutable Collections
    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")

    // Common Definitions
    implementation(project(":compiler:common"))
}
