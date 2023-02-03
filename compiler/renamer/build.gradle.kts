plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:syntax"))

    // Graph Data Structures
    implementation(libs.jgrapht)
    implementation(libs.jgraphtIo)
}