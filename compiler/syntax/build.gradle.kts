plugins {
    `java-library`
    `java-project-convention`
    `java-protobuf-convention`
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))
}
