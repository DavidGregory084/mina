plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Primitive collections
    implementation(libs.fastutilCore)

    // Pretty printing
    api(libs.prettier4j)

    // Diagnostic types
    api(libs.yvette)
}
