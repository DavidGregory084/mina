plugins {
    `java-library`
    id("java-project-convention")
    id("java-protobuf-convention")
}

dependencies {
    // Common Definitions
    api(project(":compiler:mina-compiler-common"))

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Primitive Collections
    implementation(libs.fastutilCore)
}

testing {
    suites {
        val test by
            getting(JvmTestSuite::class) {
                dependencies {
                    // Syntax Node Generators
                    implementation(project(":compiler:mina-compiler-testing"))
                }
            }
    }
}
