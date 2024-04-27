plugins {
    `java-library`
    id("java-project-convention")
    id("java-protobuf-convention")
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))
}
