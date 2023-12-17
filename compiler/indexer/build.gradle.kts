plugins {
    `java-library`
    `java-project-convention`
    `java-protobuf-convention`
}

dependencies {
    // Syntax Tree Definitions
    implementation(project(":compiler:mina-compiler-syntax"))
}
