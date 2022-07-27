plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Tree Definitions
    implementation(project(":compiler:syntax"))
}