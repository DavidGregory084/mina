plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))
}
