plugins {
    `java-library`
    `java-project-convention`
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:syntax"))

    // Java Bytecode Generation
    api(libs.asm)
    api(libs.asmCommons)
    api(libs.asmUtil)
}