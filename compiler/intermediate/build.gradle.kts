plugins {
    `java-library`
    id("java-project-convention")
    id("java-protobuf-convention")
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))

    // String Escaping
    implementation(libs.apacheCommonsText)
}
