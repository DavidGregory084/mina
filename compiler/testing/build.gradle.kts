plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Intermediate Language Definitions
    api(project(":compiler:mina-compiler-intermediate"))

    // Property-based Testing
    api(libs.jqwik)
}
