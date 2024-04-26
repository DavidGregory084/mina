plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))
}
