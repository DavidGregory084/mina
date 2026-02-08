plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // HTML templating
    implementation(libs.mustacheJava)

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Intermediate Language Definitions
    api(project(":compiler:mina-compiler-intermediate"))

    // Graph Data Structures
    api(libs.jgrapht)
    api(libs.jgraphtIo)

    // Logging
    implementation(libs.slf4jApi)
}
