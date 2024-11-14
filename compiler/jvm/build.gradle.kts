plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Java Bytecode Generation
    api(libs.asm)
    api(libs.asmCommons)
    api(libs.asmUtil)
}

testing {
    suites {
        val test by
            getting(JvmTestSuite::class) {
                dependencies {
                    // Syntax Node Generators
                    implementation(project(":compiler:mina-compiler-testing"))

                    // Runtime (needed to verify classes used in codegen)
                    implementation(project(":mina-runtime"))

                    // Failable Streams
                    implementation(libs.apacheCommonsLang)
                }
            }
    }
}
