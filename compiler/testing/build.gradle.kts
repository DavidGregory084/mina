plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Property-based Testing
    api(libs.jqwik)

    // Unicode Utilities
    implementation(libs.icu4j)
}
